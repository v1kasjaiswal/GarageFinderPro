package dev.falcon.garagefinderpro

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.karn.notify.Notify

class OwnerHomeActivity : Fragment() {

    lateinit var garageSwitch : MaterialSwitch
    lateinit var garageStatus : TextView

    lateinit var homeGarageName : TextView

    lateinit var totalRequests : TextView
    lateinit var totalJobcards : TextView

    lateinit var totalEarnings : TextView

    lateinit var jobcardOnPending : TextView
    lateinit var jobcardOnGoing : TextView
    lateinit var jobcardCompleted : TextView

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<InProgressRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.ownerhome_activity, container, false)

        homeGarageName = view.findViewById(R.id.homeGarageName)

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

                homeGarageName.text = "Welcome!, "+it.get("name").toString()
                homeGarageName.isSelected = true
            }

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

        totalRequests = view.findViewById(R.id.totalRequests)
        totalJobcards = view.findViewById(R.id.totalJobcards)

        jobcardOnPending = view.findViewById(R.id.jobcardOnPending)
        jobcardOnGoing = view.findViewById(R.id.jobcardOnGoing)
        jobcardCompleted = view.findViewById(R.id.jobcardCompleted)

        totalEarnings = view.findViewById(R.id.totalEarnings)

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereEqualTo("status", "Completed")
            .whereEqualTo("payment", "Paid")
            .get()
            .addOnSuccessListener {
                var total = 0
                for(i in it){
                    total += i.get("finalAmount").toString().toInt()
                }
                totalEarnings.text = "Total Earnings: Rs. " + total.toString()
            }

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereIn("status", listOf("Pending", "In Progress", "Completed"))
            .get()
            .addOnSuccessListener {
                totalJobcards.text = "Total Jobcards: 00"
                totalJobcards.text = "Total Jobcards: " + it.size().toString()
            }
            .addOnFailureListener {
                totalJobcards.text = "Total Jobcards: 00"
            }

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                totalRequests.text = "Total Requests: " + it.size().toString()
            }
            .addOnFailureListener {
                totalRequests.text = "Total Requests: 00"
            }

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener {
                jobcardOnPending.text = it.size().toString()
            }
            .addOnFailureListener {
                jobcardOnPending.text = "00"
            }

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereEqualTo("status", "In Progress")
            .get()
            .addOnSuccessListener {
                jobcardOnGoing.text = it.size().toString()
            }
            .addOnFailureListener {
                jobcardOnGoing.text = "00"
            }

        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereEqualTo("status", "Completed")
            .get()
            .addOnSuccessListener {
                jobcardCompleted.text = it.size().toString()
            }
            .addOnFailureListener {
                jobcardCompleted.text = "00"
            }

        recyclerview = view.findViewById(R.id.inProgressRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = InProgressRecyclerAdapter()

        recyclerview.adapter = adapter


        Log.d("TAgggG", adapter!!.itemCount.toString())

        return view
    }

}