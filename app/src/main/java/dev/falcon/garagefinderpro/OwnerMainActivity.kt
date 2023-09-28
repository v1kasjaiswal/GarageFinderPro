package dev.falcon.garagefinderpro

import android.app.NotificationManager
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class OwnerMainActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient

    lateinit var bottomnavbar : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ownermain_activity)

        val uid = auth.currentUser?.uid

        val db = Firebase.firestore

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.areNotificationsEnabled()) {
            val snackbar = Snackbar.make(findViewById(R.id.container), "Enable Notifications", Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("Enable") {
                val intent = Intent()
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
                startActivity(intent)
            }
            snackbar.show()
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                val token = it.result.toString()

                if (uid != null) {
                    db.collection("users").document(uid)
                        .update("token", token)
                        .addOnSuccessListener {
                            Log.d("TAG", "Token Updated")
                        }
                        .addOnFailureListener {
                            Log.d("TAG", "Token Update Failed")
                        }
                } else {
                    Log.d("TAG", "User not authenticated")
                }
            }

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
                    bottomnavbar.menu.findItem(R.id.requestsjobs).icon = resources.getDrawable(R.drawable.job)
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
                    bottomnavbar.menu.findItem(R.id.requestsjobs).icon = resources.getDrawable(R.drawable.job)
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
                    bottomnavbar.menu.findItem(R.id.requestsjobs).icon = resources.getDrawable(R.drawable.job)
                    bottomnavbar.menu.findItem(R.id.insights).icon = resources.getDrawable(R.drawable.insights_dark)
                    true
                }
                R.id.requestsjobs -> {
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .replace(R.id.container, OwnerRequestsJobsActivity())
                        .commit()
                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)
                    bottomnavbar.menu.findItem(R.id.requestsjobs).icon = resources.getDrawable(R.drawable.job_dark)
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

            val db = Firebase.firestore

            FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid)
                            .update("token", "null")
                            .addOnSuccessListener {
                                Log.d("TAG", "Token Deleted")
                                auth.signOut()
                                googleSignInClient.signOut()
                                val intent = Intent(this@OwnerMainActivity, SignInActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Log.d("TAG", "Token Deletion Failed")
                            }
                    } else {
                        Log.d("TAG", "User not authenticated")
                    }
                }
                .addOnFailureListener {
                    Log.d("TAG", "Token Deletion Failed")
                }



        } catch (e: Exception) {
            Toast.makeText(this, "Error in Sign-out", Toast.LENGTH_SHORT).show()
        }
    }
}