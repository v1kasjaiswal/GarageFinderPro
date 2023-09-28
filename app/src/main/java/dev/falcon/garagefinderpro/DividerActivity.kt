package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DividerActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.divider_activity)

        networkReceiver = CheckConnectivity()

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

    lateinit var radio1 : RadioButton
    lateinit var radio2 : RadioButton

    fun openGenerateUser(view: View) {

        radio1 = findViewById(R.id.radioButton3)
        radio2 = findViewById(R.id.radioButton4)

        var whouser = if (radio1.isChecked) "user" else if (radio2.isChecked) "garageowner" else "none"

        if (whouser == "none") {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
        } else {
            val bundle = intent.extras

            val googleSignInRequest = bundle?.getString("googleSignInRequest", "false")
            Log.d("googleSignInRequest",googleSignInRequest.toString())


            if (googleSignInRequest == "true") {
                val googleSignInEmail = bundle?.getString("googleSignInEmail")
                val googleSignInName = bundle?.getString("googleSignInName")
                val googleSignInPhoto = bundle?.getString("googleSignInPhoto")

                val db = Firebase.firestore

                val userMap = hashMapOf(
                    "type" to whouser,
                    "name" to googleSignInName,
                    "email" to googleSignInEmail,
                    "photo" to googleSignInPhoto
                )

                val user = Firebase.auth.currentUser

                db.collection("users").document(user!!.uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        if (whouser == "user") {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            val intent = Intent(this@DividerActivity, UserMainActivity::class.java)
                            startActivity(intent)
                        } else {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            val intent = Intent(this@DividerActivity, OwnerMainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
            val intent = Intent(this@DividerActivity, SignUpActivity::class.java)
            intent.putExtra("usertype", whouser.toString())
            startActivity(intent)
            }
        }
    }

    fun dividerCancel(view: View) {
        finish()
    }

    fun openDividerSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }
}