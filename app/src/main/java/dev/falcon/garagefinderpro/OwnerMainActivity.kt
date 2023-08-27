package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class OwnerMainActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ownermain_activity)

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
            .add(R.id.container, OwnerHomeActivity())
            .commit()

        Log.d("active AcTIVYT", "OWNERMAIN ACTIVITY")

        networkReceiver = CheckConnectivity()

        val gsio = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gsio)


        Log.d("TAG", "onCreate: ${auth.currentUser?.displayName}")

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

    fun signOut(view: View) {
        try {
            auth.signOut()
            googleSignInClient.signOut()

            val intent = Intent(this@OwnerMainActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error in Sign-out", Toast.LENGTH_SHORT).show()
        }
    }
}