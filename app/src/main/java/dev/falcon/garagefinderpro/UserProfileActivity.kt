package dev.falcon.garagefinderpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserProfileActivity : Fragment() {

    lateinit var userName : TextView

//    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.userprofile_activity, container, false)

        userName = view.findViewById(R.id.userName)
        userName.isSelected = true

//        var auth = FirebaseAuth.getInstance()

//        var utilities = Utilities()

//        get the name from the firebase firestore and set it to the userName textview
//        db.collection("users").document(auth.currentUser!!.uid)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    val salt = ByteArray(16)
//                    val iterations = 10000
//                    val keyLength = 256
//                    val initializationVector = utilities.generateIV()
//                    val secretPass = resources.getString(R.string.secretPass)
//
//                    var userName = utilities.decryptData(document.data?.get("name").toString(), secretPass, salt, iterations, keyLength, initializationVector)
//                    this.userName.text = userName
//                } else {
//                    Toast.makeText(context, "No such document", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(context, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
//            }

        return view
    }

}