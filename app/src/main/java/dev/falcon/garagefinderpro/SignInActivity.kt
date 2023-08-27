package dev.falcon.garagefinderpro

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

private const val RC_SIGN_IN = 2048

class SignInActivity : AppCompatActivity() {
    lateinit var networkReceiver : CheckConnectivity
    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin_activity)

        networkReceiver = CheckConnectivity()

        val gsio = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gsio)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    fun openSignInSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

    fun masterCancel(view: View) {
        Toast.makeText(this, "Master Cancel", Toast.LENGTH_SHORT).show()
    }

    fun openDivider(view: View) {
        val intent = Intent(this, DividerActivity::class.java)
        startActivity(intent)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(this)

                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                Log.d("signInWithCredential:success222", credential.toString())
//                if (credential != null) {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val user = auth.currentUser

                            if (user != null) {
                                db.collection("users").document(user!!.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            val type = document.data?.get("type").toString()
                                            if (type == "user") {

                                                val intent = Intent(this@SignInActivity, UserMainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                                Log.d(
                                                    "username",
                                                    "DocumentSnapshot data: ${document.data}"
                                                )
                                            } else if (type == "garageowner") {
                                                val intent = Intent(this, OwnerMainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                val intent =
                                                    Intent(this, DividerActivity::class.java)
                                                intent.putExtra(
                                                    "googleSignInCredential",
                                                    credential.toString()
                                                )
                                                intent.putExtra("googleSignInRequest", "true")
                                                intent.putExtra("googleSignInEmail", user.email)
                                                intent.putExtra(
                                                    "googleSignInName",
                                                    user.displayName
                                                )
                                                intent.putExtra(
                                                    "googleSignInPhoto",
                                                    user.photoUrl.toString()
                                                )
                                                intent.putExtra("googleSignInId", user.uid)
                                                startActivity(intent)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("TAG", "User Not Found!", exception)
                                    }
                            } else {
                                val intent = Intent(this, DividerActivity::class.java)
                                intent.putExtra("googleSignInRequest", "true")
                                intent.putExtra("googleSignInEmail", user?.email)
                                intent.putExtra("googleSignInName", user?.displayName)
                                intent.putExtra(
                                    "googleSignInPhoto",
                                    user?.photoUrl.toString()
                                )
                                intent.putExtra("googleSignInId", user?.uid)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this@SignInActivity, "Error in Sign-In With Google", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this@SignInActivity, "Error in Sign-In With Google", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error in Sign-in with Google", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    fun signInWithGoogle(view: View) {
        view.isEnabled = false
        try
        {
            val signInIntent = googleSignInClient.signInIntent

            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        catch (e:Exception)
        {
            Toast.makeText(this, "Error in Sign-in with Google", Toast.LENGTH_SHORT).show()
        }
        finally {
            view.isEnabled = true
        }
    }

    lateinit var email : EditText
    lateinit var password : EditText

    fun signIn(view: View) {

        email = findViewById(R.id.signinemail)
        password = findViewById(R.id.signinpass)

        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (emailText.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(emailText).matches())
        {
            if (passwordText.isNotBlank() && Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$", passwordText))
            {
                auth.fetchSignInMethodsForEmail(emailText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            val signInMethods = result?.signInMethods
                            if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {

                                auth.signInWithEmailAndPassword(emailText,passwordText)
                                    .addOnCompleteListener(this) { task ->

                                        var user=auth.currentUser

                                        if (user!=null && user.isEmailVerified) {
                                            if (task.isSuccessful) {
                                                var userUid = user.uid

                                                Log.d("TAG2", "onCreate: $userUid")

                                                var userRef = db.collection("users").document(userUid.toString())

                                                Log.d("TAG", "DocumentSnapshot  HERE data: $userRef")

                                                userRef.get()
                                                    .addOnSuccessListener { document ->
                                                        if (document != null) {
                                                            Log.d("TAG", "DocumentSnapshot ddd HEREEEEE data: ${document.data}")
                                                            if (document.data?.get("type") == "garageowner") {

                                                                Log.d("TAG", "DocumentSnapshot  HEREEEEE data: ${document.data}")
                                                                val intent = Intent(this@SignInActivity, OwnerMainActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            } else if (document.data?.get("type") == "user") {
                                                                Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                                                val intent = Intent(this@SignInActivity, UserMainActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                        } else {
                                                            Toast.makeText(this@SignInActivity, "No User Found", Toast.LENGTH_SHORT).show()

                                                        }
                                                    }
                                                    .addOnFailureListener {

                                                        Toast.makeText(this@SignInActivity, "Error ${it.toString()}", Toast.LENGTH_SHORT).show()

                                                    }
                                            }
                                            else
                                            {
                                                email.error = "Email or password is incorrect"
                                                email.requestFocus()
                                            }
                                        }
                                        else
                                        {
                                            email.error = "Email is not verified"
                                            email.requestFocus()
                                        }
                                    }
                                    .addOnFailureListener(this) {
                                        email.error = "Email or password is incorrect"
                                        email.requestFocus()
                                    }

                            }
                            else
                            {
                                email.error = "Account does not exist"
                                email.requestFocus()
                            }
                        }
                        else
                        {
                            email.error = "Email is not registered"
                            email.requestFocus()
                        }

                    }
                    .addOnFailureListener(this) {
                        email.error = "Email or password is incorrect"
                        email.requestFocus()
                    }
            }
            else
            {
                password.error = "Password is Invalid/Incorrect"
                password.requestFocus()
            }
        }
        else
        {
            email.error = "Email is required"
            email.requestFocus()
        }
    }

    fun forgotPassword(view: View) {

        email = findViewById(R.id.signinemail)

        if (email.text.toString().isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches())
        {
            auth.fetchSignInMethodsForEmail(email.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        val result = task.result
                        val signInMethods = result?.signInMethods
                        if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                        {
                            auth.sendPasswordResetEmail(email.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful)
                                    {
                                        Snackbar.make(findViewById(R.id.container), "Password Reset Email Sent", Snackbar.LENGTH_SHORT).show()
                                    }
                                    else
                                    {
                                        email.error = "Email is not registered"
                                        email.requestFocus()
                                    }
                                }
                        }
                        else
                        {
                            email.error = "Email is not registered"
                            email.requestFocus()
                        }
                    }
                    else
                    {
                        email.error = "Email is not registered"
                        email.requestFocus()
                    }
                }
        }
        else
        {
            email.error = "Please enter a valid Email"
            email.requestFocus()
        }
    }

    fun signinCancel(view: View) {
        finish()
    }
}