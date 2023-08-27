package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mappls.sdk.maps.Mappls
import com.mappls.sdk.maps.MapplsMap
import com.mappls.sdk.services.account.MapplsAccountManager


class UserMainActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient

    lateinit var bottomnavbar : BottomNavigationView

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usermain_activity)

        MapplsAccountManager.getInstance().restAPIKey = resources.getString(R.string.mapsdkkey)
        MapplsAccountManager.getInstance().mapSDKKey = resources.getString(R.string.mapsdkkey)
        MapplsAccountManager.getInstance().atlasClientId = resources.getString(R.string.clientid)
        MapplsAccountManager.getInstance().atlasClientSecret = resources.getString(R.string.clientsecret)
        MapplsAccountManager.getInstance().setRegion("IND");
        Mappls.getInstance(applicationContext)

        val gsio = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .build()

        networkReceiver = CheckConnectivity()
        googleSignInClient = GoogleSignIn.getClient(this, gsio)

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .add(R.id.container, UserHomeActivity())
            .commit()

        Log.d("TAG", "onCreate: ${auth.currentUser?.displayName}")

        bottomnavbar = findViewById(R.id.bottomnavbar)

        bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home_dark)

        bottomnavbar.setOnItemSelectedListener { item ->
            when (item.itemId)
            {
                R.id.home -> {
//                    when the user clicks on the home icon change the icon and when the user goes to another icon change the icon back to the original icon
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home_dark)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserHomeActivity())
                        .commit()
                    true
                }
                R.id.search -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search_dark)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserSearchActivity())
                        .commit()
                    true
                }
                R.id.navigation -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker_dark)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)


                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserNavigationActivity())
                        .commit()

                    true
                }
                R.id.notifications -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell_dark)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserNotificationActivity())
                        .commit()
                    true
                }
                R.id.profile -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.notifications).icon = resources.getDrawable(R.drawable.bell)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user_dark)


                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserProfileActivity())
                        .commit()
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

            val intent = Intent(this@UserMainActivity, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

}