package dev.falcon.garagefinderpro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailsReviewRecyclerAdapter(garageId: String) : RecyclerView.Adapter<DetailsReviewRecyclerAdapter.ViewHolder>()
{
    var detailsReviewNames = listOf<String>()
    var detailsReviewRatings = listOf<String>()
    var detailsReviewComments = listOf<String>()
    var detailsReviewDates = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        Log.d("TAG", "onCreateView: $garageId")
        db.collection("jobcards")
            .whereEqualTo("garageId", garageId)
            .whereEqualTo("status", "Completed")
            .whereNotEqualTo("review", null)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    detailsReviewNames = detailsReviewNames + document.data["name"].toString()
                    detailsReviewRatings = detailsReviewRatings + document.data["rating"].toString()
                    detailsReviewComments = detailsReviewComments + document.data["review"].toString()
                    detailsReviewDates = detailsReviewDates + document.data["date"].toString()
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "onCreateView: ${it.message}")
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsReviewRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.detailsreview_resource, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.d("TAG", "getItemCount: ${detailsReviewNames.size}")

        return detailsReviewNames.size
    }

    override fun onBindViewHolder(holder: DetailsReviewRecyclerAdapter.ViewHolder, position: Int) {
        holder.detailsReviewName.text = detailsReviewNames[position]
        holder.detailsReviewRating.text = detailsReviewRatings[position]+"★"
        holder.detailsReviewComment.text = detailsReviewComments[position]
        holder.detailsReviewDate.text = detailsReviewDates[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var detailsReviewName : TextView
        lateinit var detailsReviewRating : TextView
        lateinit var detailsReviewComment : TextView
        lateinit var detailsReviewDate : TextView

        init {
            detailsReviewName = itemView.findViewById(R.id.detailsReviewName)
            detailsReviewRating = itemView.findViewById(R.id.detailsReviewRating)
            detailsReviewComment = itemView.findViewById(R.id.detailsReviewComment)
            detailsReviewDate = itemView.findViewById(R.id.detailsReviewDate)
        }

    }
}