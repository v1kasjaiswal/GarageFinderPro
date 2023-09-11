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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class OwnerMainActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient

    lateinit var bottomnavbar : BottomNavigationView

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

        bottomnavbar = findViewById(R.id.bottomnavbarowner)

        bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home_dark)

        bottomnavbar.setOnItemSelectedListener {item ->
            when (item.itemId){
                R.id.home -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .replace(R.id.container, OwnerHomeActivity())
                        .commit()
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home_dark)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.insights).icon = resources.getDrawable(R.drawable.insights)
                    true
                }
                R.id.profile -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .replace(R.id.container, OwnerProfileActivity())
                        .commit()
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user_dark)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.insights).icon = resources.getDrawable(R.drawable.insights)
                    true
                }
                R.id.insights -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .replace(R.id.container, OwnerInsightsActivity())
                        .commit()
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.insights).icon = resources.getDrawable(R.drawable.insights_dark)
                    true
                }
                R.id.notifications -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .replace(R.id.container, UserNotificationActivity())
                        .commit()
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell_dark)
                    bottomnavbar.menu.findItem(R.id.insights).icon = resources.getDrawable(R.drawable.insights)
                    true
                }

                else -> false
            }
        }

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