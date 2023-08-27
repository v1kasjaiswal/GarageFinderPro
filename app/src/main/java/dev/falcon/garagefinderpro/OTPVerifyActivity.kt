package dev.falcon.garagefinderpro

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class OTPVerifyActivity : AppCompatActivity() {

    lateinit var networkReceiver : CheckConnectivity

    var auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otpverify_activity)

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

    fun otpCancel(view: View) {
        finish()
    }

    fun openOTPSupport(view: View) {
        val intent = Intent(this, SupportActivity::class.java)
        startActivity(intent)
    }

    lateinit var contactInput : EditText
    lateinit var storedVerificationId : String
    lateinit var sendOTPButton : Button

    fun sendOTP(view: View) {
        contactInput = findViewById(R.id.signupcontact)

        sendOTPButton = findViewById(R.id.sendOTPButton)

        if (contactInput.text.toString().isNotBlank() && Patterns.PHONE.matcher(contactInput.text.toString()).matches() && contactInput.text.toString().length == 10) {
            val contact = "+91"+contactInput.text.toString()

            val options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(contact)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Toast.makeText(this@OTPVerifyActivity, "Verification Completed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onVerificationFailed(p0: com.google.firebase.FirebaseException) {
                        Toast.makeText(this@OTPVerifyActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        storedVerificationId = verificationId

                        Log.d("VerificationID", storedVerificationId.toString())

                        sendOTPButton.setBackgroundColor(resources.getColor(R.color.offline))
                        sendOTPButton.isEnabled = false
                        contactInput.isEnabled = false

                        Toast.makeText(this@OTPVerifyActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        else
        {
            contactInput.error = "Invalid Contact Number"
            contactInput.requestFocus()
        }
    }

    lateinit var otp : EditText

    fun verifyOTP(view: View) {
        otp = findViewById(R.id.signupotp)

        try {
            if (otp.text.toString().isNotBlank()) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp.text.toString())

                val bundle = intent.extras
                val usertype = bundle?.getString("usertype")
                val googleSignInRequest = bundle?.getString("googleSignInRequest", "false")
                val googleSignInEmail = bundle?.getString("googleSignInEmail")
                val googleSignInName = bundle?.getString("googleSignInName")
                val googleSignInPhoto = bundle?.getString("googleSignInPhoto")

                Log.d("PhoneCredentail", credential.toString())

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val account = GoogleSignIn.getLastSignedInAccount(this)

                            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                            auth.signInWithCredential(credential)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Verification Successful",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    Log.d("GoogleSignINREQUEST", googleSignInRequest.toString())

                                    if (googleSignInRequest == "true") {
                                        val user = auth.currentUser

                                        var utilities = Utilities()

                                        val salt = ByteArray(16)
                                        val iterations = 10000
                                        val keyLength = 256
                                        val initializationVector = utilities.generateIV()
                                        val secretPass = resources.getString(R.string.secretPass)

                                        val encryptedName =
                                            utilities.encryptData(
                                                googleSignInName.toString(),
                                                secretPass,
                                                salt,
                                                iterations,
                                                keyLength,
                                                initializationVector
                                            )

                                        val encryptedEmail =
                                            utilities.encryptData(
                                                googleSignInEmail.toString(),
                                                secretPass,
                                                salt,
                                                iterations,
                                                keyLength,
                                                initializationVector
                                            )

                                        val encryptedContact =
                                            utilities.encryptData(
                                                contactInput.text.toString(),
                                                secretPass,
                                                salt,
                                                iterations,
                                                keyLength,
                                                initializationVector
                                            )

                                        val encryptedPhoto =
                                            utilities.encryptData(
                                                googleSignInPhoto.toString(),
                                                secretPass,
                                                salt,
                                                iterations,
                                                keyLength,
                                                initializationVector
                                            )


                                        val userMap = hashMapOf(
                                            "type" to usertype,
                                            "name" to encryptedName,
                                            "email" to encryptedEmail,
                                            "contact" to encryptedContact,
                                            "photo" to encryptedPhoto
                                        )

                                        db.collection("users").document(user!!.uid)
                                            .set(userMap)
                                            .addOnSuccessListener {
                                                if (usertype == "user") {
                                                    val intent = Intent(this, UserMainActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                    finish()
                                                } else if (usertype == "garageowner") {
                                                    val intent = Intent(this, OwnerMainActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    "Error in Sign-up",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }

                                    } else {
                                        val intent = Intent(this, SignUpActivity::class.java)
                                        intent.putExtra("usertype", usertype.toString())
                                        intent.putExtra("contact", contactInput.text.toString())
                                        startActivity(intent)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
                                }

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
                    }

            } else
            {
                otp.error = "Invalid OTP"
                otp.requestFocus()
            }

        }
        catch (e: Exception) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            Log.d("TAG", "verifyOTP: ${e.toString()}")
        }
    }



    fun changeMobileNumber(view: View) {
        try {
            sendOTPButton.isEnabled = true
            sendOTPButton.setBackgroundColor(resources.getColor(R.color.black))
            contactInput.isEnabled = true
        }
        catch (e: Exception) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

    fun resendOTP(view: View) {
        try {
            sendOTPButton.isEnabled = true
            sendOTP(view)
        }
        catch (e: Exception) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }
}