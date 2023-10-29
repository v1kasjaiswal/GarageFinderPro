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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class UserMainActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    lateinit var googleSignInClient : GoogleSignInClient

    lateinit var bottomnavbar : BottomNavigationView

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usermain_activity)

        val uid = auth.currentUser?.uid

        val db = Firebase.firestore

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
                    bottomnavbar.menu.findItem(R.id.requests).icon = resources.getDrawable(R.drawable.job)
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
                    bottomnavbar.menu.findItem(R.id.requests).icon = resources.getDrawable(R.drawable.job)
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
                    bottomnavbar.menu.findItem(R.id.requests).icon = resources.getDrawable(R.drawable.job)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)


                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserNavigationActivity())
                        .commit()

                    true
                }
                R.id.requests -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.requests).icon = resources.getDrawable(R.drawable.job_dark)
                    bottomnavbar.menu.findItem(R.id.profile).icon = resources.getDrawable(R.drawable.user)

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, UserRequestsActivity())
                        .commit()
                    true
                }
                R.id.profile -> {

                    bottomnavbar.menu.findItem(R.id.home).icon = resources.getDrawable(R.drawable.home)
                    bottomnavbar.menu.findItem(R.id.search).icon = resources.getDrawable(R.drawable.search)
                    bottomnavbar.menu.findItem(R.id.navigation).icon = resources.getDrawable(R.drawable.marker)
                    bottomnavbar.menu.findItem(R.id.requests).icon = resources.getDrawable(R.drawable.job)
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

                                val intent = Intent(this@UserMainActivity, SignInActivity::class.java)
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
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

    fun openMapBox(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Info")
            .setMessage("Feature is currently under development! \n \n" +
                    "Please check back later!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

//    fun openAnother(view: View) {
//        supportFragmentManager
//            .beginTransaction()
//            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//            .replace(R.id.container, ViewProductActivity())
//            .commit()
//    }

//    fun viewMoreProducts(view: View) {
//        supportFragmentManager
//            .beginTransaction()
//            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//            .replace(R.id.container, ViewProductActivity())
//            .commit()
//    }

}