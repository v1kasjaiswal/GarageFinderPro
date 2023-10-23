package dev.falcon.garagefinderpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SupportActivity : AppCompatActivity() {

    lateinit var name :  EditText
    lateinit var email : EditText
    lateinit var msg : EditText

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.support_activity)
    }

    fun supportCancel(view: View) {
        finish()
    }

    fun supportSubmit(view: View) {
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        msg = findViewById(R.id.msg)

        val nameText = name.text.toString()
        val emailText = email.text.toString()
        val msgText = msg.text.toString()

        if (nameText.isNotEmpty() || emailText.isNotEmpty() || msgText.isNotEmpty()) {
            if (nameText.matches(Regex("^(?!\\s)[a-zA-Z\\s]{2,}$"))) {
                if (emailText.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
                    if (msgText.length > 10) {
                        Toast.makeText(this@SupportActivity, "Message Sent!", Toast.LENGTH_SHORT).show()
                        finish()

                        val support = hashMapOf(
                            "name" to nameText,
                            "email" to emailText,
                            "message" to msgText
                        )
                        db.collection("support").add(support)
                            .addOnSuccessListener {
                                Toast.makeText(this@SupportActivity, "Message Sent!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@SupportActivity, "Message Failed to Send!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    else{
                        msg.error = "Message must be at least 10 characters!"
                        msg.requestFocus()
                    }
                }
                else{
                    email.error = "Invalid Email!"
                    email.requestFocus()
                }
            }
            else{
                name.error = "Invalid Name!"
                name.requestFocus()
            }
        }
        else{
            Toast.makeText(this@SupportActivity, "All fields are Required!", Toast.LENGTH_SHORT).show()
        }
    }

    fun showInfo(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Info")
            .setMessage("Welcome to Garage Finder Pro! \n\n" +
                    "We Make sure that all your queries are solved, Thank you! \n\n" +
                    "Developed By - Vikas Jaiswal\n\n" +
                    "For any queries, please contact us")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}