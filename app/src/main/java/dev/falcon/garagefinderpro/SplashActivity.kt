package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.os.postDelayed
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val user = auth.currentUser
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (user != null && user.isEmailVerified) {
            moveToMainScreen()
        } else if (user != null && account != null) {
            checkUserDocument(user.uid)
        } else {
            moveToSignInScreen()
        }
    }

    private fun checkUserDocument(uid: String) {
        Handler(Looper.getMainLooper()).postDelayed({

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    handleUserType(document)
                } else {
                    Toast.makeText(this@SplashActivity, "No Such User", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@SplashActivity, "Error in Sign-in", Toast.LENGTH_SHORT).show()
            }
        }, 2500)
    }

    private fun handleUserType(document: DocumentSnapshot) {
        val userType = document.getString("type")
        val intent = when (userType) {
            "garageowner" -> Intent(this, OwnerMainActivity::class.java)
            "user" -> Intent(this, UserMainActivity::class.java)
            else -> {
                null
            }
        }

        intent?.let {
            startActivity(it)
            finish()
        }
    }

    private fun moveToSignInScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }

    private fun moveToMainScreen() {
            val userUid = auth.currentUser?.uid
            checkUserDocument(userUid.toString())
    }


}
