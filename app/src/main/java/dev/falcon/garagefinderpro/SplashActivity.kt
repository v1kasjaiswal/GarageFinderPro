package dev.falcon.garagefinderpro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    var auth = FirebaseAuth.getInstance()

    lateinit var googleSignInClient : GoogleSignInClient

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val gsio = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gsio)

        auth.addAuthStateListener { authenticate ->
            val user = authenticate.currentUser
            val account = GoogleSignIn.getLastSignedInAccount(this)

            Log.d("user", "onCreate: ${user?.uid}")
            Log.d("account", "onCreate: $account")

            if (user != null && user.isEmailVerified) {

                Log.d("TAG", "onCreate: ${user.uid}")

                Handler(Looper.getMainLooper()).postDelayed({
                    moveToMains()
                }, 1500)

            }
            else if (user != null && account != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    db.collection("users").document(user!!.uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                moveToMains()
                            } else {
                                Log.d("TAG", "No such document")
                            }
                        }
            }, 1500)
        }
        else {
                Handler(Looper.getMainLooper()).postDelayed({

                    val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1500)

            }
        }
    }

    private fun signInWithGoogleCredentials(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signInWithCredential:success")
                    moveToMains()
                } else {
                    Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun moveToMains() {

        var userUid = auth.currentUser?.uid

        Log.d("TAG2XX", "onCreate: $userUid")

        var user = auth.currentUser
        Log.d("TAG2XX", "onCreate: ${user?.displayName.toString()}")

        var userRef = db.collection("users").document(userUid.toString())

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("TAG", "DocumentSnapshot ddd HEREEEEE data: ${document.data}")
                    if (document.data?.get("type") == "garageowner") {
                        Log.d("TAG", "DocumentSnapshot  HEREEEEE777 data: ${document.data}")
                        val intent = Intent(this@SplashActivity, OwnerMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (document.data?.get("type") == "user") {
                        Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                        val intent = Intent(this@SplashActivity, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@SplashActivity, "Error3 in Sign-in", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@SplashActivity, "Error4 in Sign-in", Toast.LENGTH_SHORT).show()
            }

    }
}