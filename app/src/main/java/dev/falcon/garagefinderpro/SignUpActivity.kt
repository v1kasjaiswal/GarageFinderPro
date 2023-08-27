package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import dev.falcon.garagefinderpro.Utilities
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    var auth = FirebaseAuth.getInstance()

    lateinit var networkReceiver : CheckConnectivity

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        networkReceiver = CheckConnectivity()

        val bundle = intent.extras
        Log.d("Bundle", bundle.toString())
        Log.d("usertype", bundle?.getString("usertype").toString())

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


    lateinit var name : EditText
    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var confirmPassword : EditText
    lateinit var acceptTAC : CheckBox

    fun createAccount(view: View) {
        name = findViewById(R.id.signupname)
        email = findViewById(R.id.signupemail)
        password = findViewById(R.id.signuppassword)
        confirmPassword = findViewById(R.id.signupconfirmpassword)
        acceptTAC = findViewById(R.id.checkBox)

        if (name.text.toString().isNotBlank() && Pattern.matches("^(?!\\s)[a-zA-Z\\s]{2,}$", name.text.toString()))
        {
            if (email.text.toString().isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches())
            {
                if (password.text.toString().isNotBlank())
                {
                    if (confirmPassword.text.toString().isNotBlank())
                    {
                        if (Pattern.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", password.text.toString()))
                        {
                            if (password.text.toString() == confirmPassword.text.toString())
                            {
                                if (acceptTAC.isChecked)
                                {
                                    auth.fetchSignInMethodsForEmail(email.text.toString())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {
                                                val result = task.result
                                                val signInMethods = result?.signInMethods
                                                if (signInMethods != null && signInMethods.contains(
                                                        EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) && signInMethods.contains(
                                                        GoogleAuthProvider.PROVIDER_ID))
                                                {
                                                    email.error = "Email already registered"
                                                    email.requestFocus()
                                                }
                                                else
                                                {
                                                    auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                                                        .addOnCompleteListener(this)
                                                        { task ->
                                                            if (task.isSuccessful) {
                                                                val user = auth.currentUser

                                                                user?.sendEmailVerification()
                                                                    ?.addOnCompleteListener { task ->
                                                                        if (task.isSuccessful)
                                                                        {
                                                                            val bundle = intent.extras
                                                                            Log.d("bundle", bundle.toString())
                                                                            var whouser = bundle?.getString("usertype","null")
                                                                            Log.d("whouserw", whouser.toString())
                                                                            var contact = bundle?.getString("contact", "null")
                                                                            Log.d("contact", contact.toString())

                                                                            var utilities = Utilities()

                                                                            val salt = ByteArray(16)
                                                                            val iterations = 10000
                                                                            val keyLength = 256
                                                                            val initializationVector = utilities.generateIV()
                                                                            val secretPass = resources.getString(R.string.secretPass)

                                                                            val encryptedName =
                                                                                utilities.encryptData(name.text.toString(), secretPass, salt, iterations, keyLength, initializationVector)

                                                                            val encryptedEmail =
                                                                                utilities.encryptData(email.text.toString(), secretPass, salt, iterations, keyLength, initializationVector)

                                                                            val encryptedContact =
                                                                                utilities.encryptData(contact.toString(), secretPass, salt, iterations, keyLength, initializationVector)


                                                                            val userMap = hashMapOf(
                                                                                "type" to whouser,
                                                                                "name" to encryptedName,
                                                                                "email" to encryptedEmail,
                                                                                "contact" to encryptedContact
                                                                            )

                                                                            db.collection("users").document(auth.currentUser?.uid.toString()).set(userMap)
                                                                                .addOnSuccessListener {
                                                                                    Toast.makeText(this, "Verification Email Sent", Toast.LENGTH_SHORT).show()

                                                                                    val intent = Intent(this, SignInActivity::class.java)
                                                                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                                    startActivity(intent)
                                                                                    finish()
                                                                                }
                                                                                .addOnFailureListener {
                                                                                    Toast.makeText(this, "Error in Sign-up", Toast.LENGTH_SHORT).show()

                                                                                }

                                                                        }
                                                                        else
                                                                        {
                                                                            email.error = "Invalid Email"
                                                                            email.requestFocus()
                                                                        }
                                                                    }
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(this, "Error in Sign-up", Toast.LENGTH_SHORT).show()

                                                                if (task.exception is FirebaseAuthUserCollisionException)
                                                                {
                                                                    email.error = "Email Already Registered"
                                                                    email.requestFocus()
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                            else
                                            {
                                                email.error = "Email Already Registered"
                                                email.requestFocus()
                                            }
                                        }
                                }
                                else
                                {
                                    acceptTAC.error = "Please accept Terms and Conditions"
                                    acceptTAC.requestFocus()

                                }
                            }
                            else
                            {
                                confirmPassword.error =
                                    "Password and Confirm Password must be same"
                                confirmPassword.requestFocus()
                            }
                        }
                        else
                        {
                            password.requestFocus()

                            Toast.makeText(this, "Password must contain at least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 number and 1 special character", Toast.LENGTH_LONG).show()
                        }

                    }
                    else
                    {
                        confirmPassword.error = "Please enter Confirm Password"
                        confirmPassword.requestFocus()
                    }
                }
                else
                {
                    password.error = "Please enter Password"
                    password.requestFocus()
                }
            }
            else
            {
                email.error = "Please enter a valid Email"
                email.requestFocus()
            }
        }
        else
        {
            name.error = "Please enter a valid Name"
            name.requestFocus()
        }
    }

    fun signupCancel(view: View) {
        finish()
    }

    fun openSignUpSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

    fun moveToSignin(view: View) {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}