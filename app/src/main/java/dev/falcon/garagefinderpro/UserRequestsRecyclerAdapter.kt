package dev.falcon.garagefinderpro

import android.animation.LayoutTransition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import org.w3c.dom.Text

class UserRequestsRecyclerAdapter : RecyclerView.Adapter<UserRequestsRecyclerAdapter.ViewHolder>()
{
    var requestedDateTimes = listOf<String>()
    var requestedGarageNames = listOf<String>()
    var requestedServices = listOf<String>()
    var requestedJobStatues = listOf<String>()

    var requestedVehicleNames= listOf<String>()
    var requestedVehicleTypes = listOf<String>()
    var requestedVehicleModels = listOf<String>()
    var requestedVehicleNumbers = listOf<String>()
    var requestedFuelTypes = listOf<String>()
    var requestedTowRequires = listOf<String>()

    var garageIds = listOf<String>()

    lateinit var ratingBar: RatingBar
    lateinit var reviewText: EditText
    lateinit var submitReview: Button

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("status", "New Requests")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    requestedDateTimes += document.data["date"].toString()
                    requestedGarageNames += document.data["garageName"].toString()
                    requestedServices += document.data["serviceType"].toString()
                    requestedJobStatues += document.data["status"].toString()

                    requestedVehicleNames += document.data["vehicleName"].toString()
                    requestedVehicleTypes += document.data["vehicleType"].toString()
                    requestedVehicleModels += document.data["vehicleModel"].toString()
                    requestedVehicleNumbers += document.data["vehicleNumber"].toString()
                    requestedFuelTypes += document.data["vehicleFuelType"].toString()
                    requestedTowRequires += document.data["towLocation"].toString()


                    garageIds += document.data["garageId"].toString()
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRequestsRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.userrequests_resource, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return garageIds.size
    }

    override fun onBindViewHolder(holder: UserRequestsRecyclerAdapter.ViewHolder, position: Int) {
        holder.requestedDateTime.text = requestedDateTimes[position]
        holder.requestedGarageName.text = requestedGarageNames[position]
        holder.requestedService.text = "Service Required: " + requestedServices[position]
        holder.requestedJobStatus.text = "Status: " + requestedJobStatues[position]

        holder.requestedVehicleName.text = "Vehicle Name: " + requestedVehicleNames[position]
        holder.requestedVehicleType.text = "Vehicle Type: " + requestedVehicleTypes[position]
        holder.requestedVehicleModel.text = "Vehicle Model: " + requestedVehicleModels[position]
        holder.requestedVehicleNumber.text = "Vehicle Number: " + requestedVehicleNumbers[position]
        holder.requestedFuelType.text = "Fuel Type: " + requestedFuelTypes[position]
        holder.requestedTowRequire.text = "Tow Required: " + requestedTowRequires[position]

        if (requestedJobStatues[position]!="Completed"){
            holder.requestedCancel.visibility = View.VISIBLE
            holder.reviewButton.visibility = View.GONE
            holder.paymentButton.visibility = View.GONE
            holder.requestedFinalAmount.visibility = View.GONE
            holder.requestedPaymentStatus.visibility = View.GONE

            holder.requestedRating.visibility = View.GONE
            holder.requestedReview.visibility = View.GONE
        }
        else{
            holder.requestedCancel.visibility = View.GONE
            holder.reviewButton.visibility = View.VISIBLE
            holder.paymentButton.visibility = View.VISIBLE
            holder.requestedFinalAmount.visibility = View.VISIBLE
            holder.requestedPaymentStatus.visibility = View.VISIBLE

            db.collection("jobcards")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .whereEqualTo("garageId", garageIds[position])
                .whereEqualTo("date", requestedDateTimes[position])
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (document.data["review"] == null){
                            holder.reviewButton.visibility = View.VISIBLE
                            holder.requestedRating.visibility = View.GONE
                            holder.requestedReview.visibility = View.GONE
                        }
                        else{
                            holder.reviewButton.visibility = View.GONE
                            holder.requestedRating.visibility = View.VISIBLE
                            holder.requestedReview.visibility = View.VISIBLE

                            holder.requestedRating.text = "Rating: " + document.data["rating"].toString()
                            holder.requestedReview.text = "Review: \n" + document.data["review"].toString()
                        }

                        if (document.data["payment"]==null){
                            holder.paymentButton.visibility = View.VISIBLE
                            holder.requestedFinalAmount.visibility = View.VISIBLE
                            holder.requestedPaymentStatus.text = "Payment Status: Not Paid"
                        }
                        else{
                            holder.paymentButton.visibility = View.GONE
                            holder.requestedFinalAmount.visibility = View.VISIBLE
                            holder.requestedPaymentStatus.text = "Payment Status: Paid"
                        }

                        if (document.data["finalAmount"]==null){
                            holder.requestedFinalAmount.text = "Final Amount: TBD"
                        }
                        else{
                            holder.requestedFinalAmount.text = "Final Amount: " + document.data["finalAmount"].toString()
                        }

                        if (requestedJobStatues[position]=="Cancelled" || requestedJobStatues[position]=="Declined" || requestedJobStatues[position]=="Completed" ){
                            holder.requestedCancel.visibility = View.GONE
                        }
                        else
                        {
                            holder.requestedCancel.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error getting documents: ", exception)
                }
        }

        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("garageId", garageIds[position])
            .whereEqualTo("date", requestedDateTimes[position])
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.data["status"] == "Cancelled" || document.data["status"]=="Declined" || document.data["status"]=="Completed") {
                        holder.requestedCancel.visibility = View.GONE
                    }
                    else
                    {
                        holder.requestedCancel.visibility = View.VISIBLE
                    }
                }
            }

        holder.paymentButton.setOnClickListener {
            val alterDialog = MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Payment")
                .setMessage("Make Payment of ${holder.requestedFinalAmount.text.toString()}")
                .setPositiveButton("Pay") { _, _ ->
                    db.collection("jobcards")
                        .whereEqualTo("userId", auth.currentUser?.uid)
                        .whereEqualTo("garageId", garageIds[position])
                        .whereEqualTo("date", requestedDateTimes[position])
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("jobcards").document(document.id)
                                    .update("payment", "Paid")
                                    .addOnSuccessListener {
                                        Log.d("TAG", "DocumentSnapshot successfully updated!")
                                        holder.requestedPaymentStatus.text = "Payment Status: Paid"
                                        holder.paymentButton.visibility = View.GONE

                                        sendPaymentNotification(garageIds[position])
                                    }
                                    .addOnFailureListener {
                                        Log.d("TAG", "DocumentSnapshot failed to update!")
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("TAG", "Error getting documents: ", exception)
                        }
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.requestedMoreInfo.setOnClickListener {
            if (holder.requestedMoreDetails.visibility == View.GONE) {
                holder.requestedMoreDetails.visibility = View.VISIBLE
                holder.requestedMoreInfo.text = "Less Info"
            } else {
                holder.requestedMoreDetails.visibility = View.GONE
                holder.requestedMoreInfo.text = "More Info"
            }
        }

        holder.requestedCancel.setOnClickListener {
            db.collection("jobcards")
                .whereNotEqualTo("status", "Completed")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .whereEqualTo("garageId", garageIds[position])
                .whereEqualTo("date", requestedDateTimes[position])
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("jobcards").document(document.id)
                            .update("status", "Cancelled")
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully updated!")
                                holder.requestedMoreDetails.visibility = View.GONE
                                holder.requestedMoreInfo.text = "More Info"

                                sendCancelNotification(garageIds[position], "Cancelled")

                                filterRequestedData(requestedJobStatues[position])
                            }
                            .addOnFailureListener {
                                Log.d("TAG", "DocumentSnapshot failed to update!")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Error getting documents: ", exception)
                }
        }

        var bottomSheetDialog = BottomSheetDialog(holder.itemView.context)
        val bottomSheetView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.leavereview_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        holder.reviewButton.setOnClickListener {
            bottomSheetDialog.show()

            var garageId = garageIds[position]

            ratingBar = bottomSheetView.findViewById(R.id.ratingBar)
            reviewText = bottomSheetView.findViewById(R.id.reviewText)

            submitReview = bottomSheetView.findViewById(R.id.submitReview)

            submitReview.setOnClickListener {

            if (reviewText.text.toString().isNotEmpty() && reviewText.text.toString().length >= 10){
                db.collection("jobcards")
                    .whereEqualTo("userId", auth.currentUser?.uid)
                    .whereEqualTo("garageId", garageIds[position])
                    .whereEqualTo("date", requestedDateTimes[position])
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            db.collection("jobcards").document(document.id)
                                .update("review", reviewText.text.toString(),
                        "rating", ratingBar.rating)
                                .addOnSuccessListener {
                                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                                    bottomSheetDialog.dismiss()
                                    holder.requestedMoreDetails.visibility = View.GONE
                                    holder.requestedMoreInfo.text = "More Info"

                                    db.collection("users").document(garageId)
                                        .get()
                                        .addOnSuccessListener {

                                            var rating : Float
                                            var ratingCount : Int
                                            try {
                                                rating = it.data?.get("rating").toString().toFloat()
                                                ratingCount = it.data?.get("ratingCount").toString().toInt()
                                            }
                                            catch (e: Exception){
                                                rating = 0.0F
                                                ratingCount = 0
                                            }

                                            rating = (rating * ratingCount + ratingBar.rating) / (ratingCount + 1)
                                            ratingCount += 1

                                            db.collection("users").document(garageId)
                                                .update("rating", rating,
                                        "ratingCount", ratingCount)
                                                .addOnSuccessListener {
                                                    Log.d("TAG", "DocumentSnapshot successfully updated!")

                                                    sendReviewNotification(garageId, rating.toString(), reviewText.text.toString())
                                                }
                                                .addOnFailureListener {
                                                    Log.d("TAG", "DocumentSnapshot failed to update!")
                                                }
                                        }
                                        .addOnFailureListener {
                                            Log.d("TAG", "DocumentSnapshot failed to update!")
                                        }

                                }
                                .addOnFailureListener {
                                    Log.d("TAG", "DocumentSnapshot failed to update!")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "Error getting documents: ", exception)
                    }
            }
            else{
                reviewText.error = "Please enter a review of atleast 10 characters"
                reviewText.requestFocus()
            }

            }

        }
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var requestedMoreInfo : TextView
        lateinit var requestedMoreDetails : LinearLayout

        lateinit var requestedDateTime : TextView
        lateinit var requestedGarageName : TextView
        lateinit var requestedService : TextView
        lateinit var requestedJobStatus : TextView

        lateinit var requestedVehicleName : TextView
        lateinit var requestedVehicleType : TextView
        lateinit var requestedVehicleModel : TextView
        lateinit var requestedVehicleNumber : TextView
        lateinit var requestedFuelType : TextView
        lateinit var requestedTowRequire : TextView

        lateinit var requestedCancel : Button
        lateinit var reviewButton : Button

        lateinit var paymentButton : Button
        lateinit var requestedFinalAmount : TextView
        lateinit var requestedPaymentStatus : TextView

        lateinit var requestedRating: TextView
        lateinit var requestedReview: TextView



        init {
            requestedMoreInfo = itemView.findViewById(R.id.requestedMoreInfo)
            requestedMoreDetails = itemView.findViewById(R.id.requestedMoreDetails)

            requestedMoreDetails.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

            val transition = AutoTransition()
            TransitionManager.beginDelayedTransition(requestedMoreDetails, transition)

            requestedDateTime = itemView.findViewById(R.id.requestedDateTime)
            requestedGarageName = itemView.findViewById(R.id.requestedGarageName)
            requestedService = itemView.findViewById(R.id.requestedServiceRequired)
            requestedJobStatus = itemView.findViewById(R.id.requestedJobStatus)

            requestedVehicleName = itemView.findViewById(R.id.requestedVehicleName)
            requestedVehicleType = itemView.findViewById(R.id.requestedVehicleType)
            requestedVehicleModel = itemView.findViewById(R.id.requestedVehicleModel)
            requestedVehicleNumber = itemView.findViewById(R.id.requestedVehicleNumber)
            requestedFuelType = itemView.findViewById(R.id.requestedFuelType)
            requestedTowRequire = itemView.findViewById(R.id.requestedTowRequired)

            requestedCancel = itemView.findViewById(R.id.requestedCancel)

            reviewButton = itemView.findViewById(R.id.reviewButton)

            paymentButton = itemView.findViewById(R.id.paymentButton)
            requestedFinalAmount = itemView.findViewById(R.id.requestedFinalAmount)
            requestedPaymentStatus = itemView.findViewById(R.id.requestedPaymentStatus)

            requestedRating = itemView.findViewById(R.id.requestedRating)
            requestedReview = itemView.findViewById(R.id.requestedReview)

        }
    }

    fun filterRequestedData(status: String){
        requestedDateTimes = emptyList()
        requestedGarageNames = emptyList()
        requestedServices = emptyList()
        requestedJobStatues = emptyList()

        requestedVehicleNames = emptyList()
        requestedVehicleTypes = emptyList()
        requestedVehicleModels = emptyList()
        requestedVehicleNumbers = emptyList()
        requestedFuelTypes = emptyList()
        requestedTowRequires = emptyList()

        garageIds = emptyList()

        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    requestedDateTimes += document.data["date"].toString()
                    requestedGarageNames += document.data["garageName"].toString()
                    requestedServices += document.data["serviceType"].toString()
                    requestedJobStatues += document.data["status"].toString()

                    requestedVehicleNames += document.data["vehicleName"].toString()
                    requestedVehicleTypes += document.data["vehicleType"].toString()
                    requestedVehicleModels += document.data["vehicleModel"].toString()
                    requestedVehicleNumbers += document.data["vehicleNumber"].toString()
                    requestedFuelTypes += document.data["vehicleFuelType"].toString()
                    requestedTowRequires += document.data["towLocation"].toString()

                    garageIds += document.data["garageId"].toString()

                }
                notifyDataSetChanged()
            }
    }

    fun sendCancelNotification(garageId: String, status: String){
        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("garageId", garageId)
            .whereEqualTo("date", requestedDateTimes[0])
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var userName = document.data["name"].toString()
                    var serviceType = document.data.get("serviceType").toString()
                    var vehicleName = document.data.get("vehicleName").toString()
                    var vehicleNumber = document.data.get("vehicleNumber").toString()
                    var status = status
                    var notificationType = "cancelJobcard"

                    db.collection("users").document(garageId)
                        .get()
                        .addOnSuccessListener {
                            var token = it.data?.get("token").toString()

                            var jsonObject = JSONObject()
                            var jsonObjectData = JSONObject()

                            jsonObjectData.put("title", "Request/Jobcard Cancelled")
                            jsonObjectData.put("body", "Your Request/Jobcard for $vehicleNumber has been cancelled by $userName")

                            jsonObjectData.put("userName", userName)
                            jsonObjectData.put("serviceType", serviceType)
                            jsonObjectData.put("vehicleName", vehicleName)
                            jsonObjectData.put("vehicleNumber", vehicleNumber)
                            jsonObjectData.put("status", status)
                            jsonObjectData.put("notificationType", notificationType)

                            jsonObject.put("to", token)
                            jsonObject.put("data", jsonObjectData)

                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    processNotification(jsonObject)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun sendPaymentNotification(garageId: String){
        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("garageId", garageId)
            .whereEqualTo("date", requestedDateTimes[0])
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var userName = document.data["name"].toString()
                    var serviceType = document.data.get("serviceType").toString()
                    var vehicleName = document.data.get("vehicleName").toString()
                    var vehicleNumber = document.data.get("vehicleNumber").toString()
                    var status = "Completed"
                    var notificationType = "paymentJobcard"

                    db.collection("users").document(garageIds[0])
                        .get()
                        .addOnSuccessListener {
                            var token = it.data?.get("token").toString()

                            var jsonObject = JSONObject()
                            var jsonObjectData = JSONObject()

                            jsonObjectData.put("title", "Payment Received")
                            jsonObjectData.put("body", "Your Payment for $vehicleNumber has been received by $userName")

                            jsonObjectData.put("userName", userName)
                            jsonObjectData.put("serviceType", serviceType)
                            jsonObjectData.put("vehicleName", vehicleName)
                            jsonObjectData.put("vehicleNumber", vehicleNumber)
                            jsonObjectData.put("status", status)
                            jsonObjectData.put("notificationType", notificationType)

                            jsonObject.put("to", token)
                            jsonObject.put("data", jsonObjectData)

                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    processNotification(jsonObject)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun sendReviewNotification(garageId: String, rating: String, reviewText: String){
        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("garageId", garageId)
            .whereEqualTo("date", requestedDateTimes[0])
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var userName = document.data["name"].toString()
                    var vehicleName = document.data.get("vehicleName").toString()
                    var vehicleNumber = document.data.get("vehicleNumber").toString()
                    var rating = String.format("%.2f", rating.toFloat())
                    var reviewText = reviewText
                    var notificationType = "jobcardReview"

                    db.collection("users").document(garageId)
                        .get()
                        .addOnSuccessListener {
                            var token = it.data?.get("token").toString()

                            var jsonObject = JSONObject()
                            var jsonObjectData = JSONObject()

                            jsonObjectData.put("title", "New Review")
                            jsonObjectData.put("body", "$userName has left a review for your garage")

                            jsonObjectData.put("userName", userName)
                            jsonObjectData.put("vehicleName", vehicleName)
                            jsonObjectData.put("vehicleNumber", vehicleNumber)
                            jsonObjectData.put("rating", rating)
                            jsonObjectData.put("reviewText", reviewText)
                            jsonObjectData.put("notificationType", notificationType)

                            jsonObject.put("to", token)
                            jsonObject.put("data", jsonObjectData)

                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    processNotification(jsonObject)
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    fun processNotification(jsonObject: JSONObject) {
        var mediaType = "application/json; charset=utf-8".toMediaType()

        var client = OkHttpClient()

        var url = "https://fcm.googleapis.com/fcm/send"

        var body = jsonObject.toString().toRequestBody(mediaType)

        var request = okhttp3.Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer AAAAshwHFJE:APA91bFuSH0ZtxwwLZV8RAJORY6xsV15MQpRR7QohdR7lWnqHmN3mTgWtsEzUD7Y-V8U4pezDqO84Yr6EAfHdYw1mN37h8N-LqIxahAhPID422V_v2jX0AzBWyjiHPVbrtr74Uol7z8O")
            .build()

        var response = client.newCall(request).execute()

        if (response.isSuccessful){
            Log.d("TAG", "Notification sent")
        }
        else{
            Log.d("TAG", "Notification not sent")
        }
    }

}