package dev.falcon.garagefinderpro

import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.karn.notify.Notify

class OwnerHomeActivity : Fragment() {

    lateinit var garageSwitch : MaterialSwitch
    lateinit var garageStatus : TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.ownerhome_activity, container, false)

        garageSwitch = view.findViewById(R.id.garageSwitch)
        garageStatus = view.findViewById(R.id.garageStatus)

        var auth = FirebaseAuth.getInstance()

        var db = Firebase.firestore

        db.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if(it.get("garageStatus") == "Open"){
                    garageSwitch.isChecked = true
                    garageStatus.text = "Open"
                }else{
                    garageSwitch.isChecked = false
                    garageStatus.text = "Closed"
                }
            }

//        if the garageSwitch is on then change the status of the garage to open else closed and update in the database
        garageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                db.collection("users").document(auth.currentUser!!.uid)
                    .update("garageStatus", "Open")
                    .addOnSuccessListener {
                        garageStatus.text = "Open"


                    }
                    .addOnFailureListener {
                        garageSwitch.isChecked = false
                    }
            }else{
                db.collection("users").document(auth.currentUser!!.uid)
                    .update("garageStatus", "Closed")
                    .addOnSuccessListener {
                        garageStatus.text = "Closed"

                    }
                    .addOnFailureListener {
                        garageSwitch.isChecked = true
                    }
            }
        }


        return view
    }

}