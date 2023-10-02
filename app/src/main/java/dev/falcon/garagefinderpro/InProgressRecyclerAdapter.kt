package dev.falcon.garagefinderpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InProgressRecyclerAdapter : RecyclerView.Adapter<InProgressRecyclerAdapter.ViewHolder>() {

    var requestingDates = listOf<String>()
    var requestingNames = listOf<String>()
    var requestingServices = listOf<String>()
    var requestingStatus = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser!!.uid)
            .whereIn("status", listOf("New Requests","In Progress", "Pending"))
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    requestingDates = requestingDates + document.data["date"].toString()
                    requestingNames = requestingNames + document.data["name"].toString()
                    requestingServices = requestingServices + document.data["serviceType"].toString()
                    requestingStatus = requestingStatus + document.data["status"].toString()
                }
                notifyDataSetChanged()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InProgressRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ownerrequestsjobs_resource, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return requestingDates.size
    }

    override fun onBindViewHolder(holder: InProgressRecyclerAdapter.ViewHolder, position: Int) {
        holder.requestingDate.text = requestingDates[position]
        holder.requestingName.text = requestingNames[position]
        holder.requestingService.text = requestingServices[position]
        holder.requestingStatus.text = requestingStatus[position]

        holder.requestingMoreInfo.visibility = View.GONE
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var requestingDate : TextView
        lateinit var requestingName : TextView
        lateinit var requestingService : TextView
        lateinit var requestingStatus : TextView

        lateinit var requestingMoreInfo : TextView

        init{
            requestingDate = itemView.findViewById(R.id.requesterDateTime)
            requestingName = itemView.findViewById(R.id.requesterName)
            requestingService = itemView.findViewById(R.id.requesterServiceRequired)
            requestingStatus = itemView.findViewById(R.id.requesterJobStatus)

            requestingMoreInfo = itemView.findViewById(R.id.requesterMoreInfo)
        }
    }
}