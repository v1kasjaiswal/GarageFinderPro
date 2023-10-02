package dev.falcon.garagefinderpro

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import android.widget.TextView
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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
import java.text.SimpleDateFormat

class OwnerRequestsJobsRecyclerAdapter : RecyclerView.Adapter<OwnerRequestsJobsRecyclerAdapter.ViewHolder>()
{
    var requesterDateTimes = listOf<String>()
    var requesterNames = listOf<String>()
    var requesterServices  = listOf<String>()
    var requesterJobStatuses = listOf<String>()

    var requesterVehicleNames = listOf<String>()
    var requesterVehicleTypes = listOf<String>()
    var requesterVehicleModels = listOf<String>()
    var requesterVehicleNumbers = listOf<String>()
    var requesterFuelTypes = listOf<String>()
    var requesterTowRequires = listOf<String>()
    var requesterContacts = listOf<String>()

    var userIds = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("jobcards")
            .whereEqualTo("garageId", auth.currentUser?.uid.toString())
            .whereEqualTo("status", "New Requests")
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    requesterDateTimes = requesterDateTimes + document.data.get("date").toString()
                    requesterNames = requesterNames + document.data.get("name").toString()
                    requesterServices = requesterServices + document.data.get("serviceType").toString()
                    requesterJobStatuses = requesterJobStatuses + document.data.get("status").toString()

                    requesterVehicleNames = requesterVehicleNames + document.data.get("vehicleName")
                        .toString()
                    requesterVehicleTypes = requesterVehicleTypes + document.data.get("vehicleType")
                        .toString()
                    requesterVehicleModels = requesterVehicleModels + document.data.get("vehicleModel")
                        .toString()
                    requesterVehicleNumbers = requesterVehicleNumbers + document.data.get("vehicleNumber")
                        .toString()
                    requesterFuelTypes = requesterFuelTypes + document.data.get("vehicleFuelType")
                        .toString()
                    requesterTowRequires = requesterTowRequires + document.data.get("towLocation")
                        .toString()

                    userIds = userIds + document.data.get("userId").toString()

                    requesterContacts = requesterContacts + document.data.get("contact").toString()

                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }

        Log.d("Contacts", requesterContacts.toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerRequestsJobsRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ownerrequestsjobs_resource, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return requesterNames.size
    }

    override fun onBindViewHolder(holder: OwnerRequestsJobsRecyclerAdapter.ViewHolder, position: Int) {

        holder.requesterDateTime.text = requesterDateTimes[position]
        holder.requesterName.text = requesterNames[position]
        holder.requesterService.text = "Service Required: " + requesterServices[position]
        holder.requesterJobStatus.text = "Status: " + requesterJobStatuses[position]

        holder.requesterVehicleName.text = "Vehicle Name: " + requesterVehicleNames[position]
        holder.requesterVehicleType.text = "Vehicle Type: " + requesterVehicleTypes[position]
        holder.requesterVehicleModel.text = "Vehicle Model: " + requesterVehicleModels[position]
        holder.requesterVehicleNumber.text = "Vehicle Number: " + requesterVehicleNumbers[position].dropLast(2)+"XX"
        holder.requesterFuelType.text = "Fuel Type: " + requesterFuelTypes[position]
        holder.requesterTowRequired.text = "Tow Required: " + requesterTowRequires[position]

        if (requesterContacts[position] == null)
        {
            holder.requesterCall.isEnabled = false
            holder.requesterCall.alpha = 0.5f
        }
        else
        {
            holder.requesterCall.isEnabled = true
            holder.requesterCall.alpha = 1f
        }

        holder.requesterCall.setOnClickListener {
            var intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + requesterContacts[position])
            holder.itemView.context.startActivity(intent)
        }

        if (requesterTowRequires[position]=="Not Required"){
            holder.requesterMaps.isEnabled = false
            holder.requesterMaps.alpha = 0.5f
        }
        else{
            holder.requesterMaps.isEnabled = true
            holder.requesterMaps.alpha = 1f
        }

        holder.requesterMaps.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:0,0?q=" + requesterTowRequires[position])
            holder.itemView.context.startActivity(intent)
        }

        if (requesterJobStatuses[position] == "New Requests")
        {
            holder.requesterAccept.visibility = View.VISIBLE
            holder.requesterDecline.visibility = View.VISIBLE

            holder.statusesUpdateLayout.visibility = View.GONE
            holder.requesterUpdate.visibility = View.GONE

        }

        if (requesterJobStatuses[position] == "Completed" || requesterJobStatuses[position] == "Declined")
        {
            holder.requesterUpdate.visibility = View.GONE
            holder.statusesUpdateLayout.visibility = View.GONE
        }

//        if the job status is Completed then show the rating text and review text if review is not null and hide the review button
//        if the job status is not Completed then hide the rating text and review text and show the review button
//        if the job status is Declined then hide the rating text and review text and show the review button
//        if the job status is Cancelled then hide the rating text and review text and show the review button
        db.collection("jobcards")
            .whereEqualTo("userId", userIds[position])
            .whereEqualTo("garageId", auth.currentUser?.uid.toString())
            .whereEqualTo("date", requesterDateTimes[position])
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    if (document.data.get("status").toString() == "Completed")
                    {
                        holder.requesterRating.visibility = View.VISIBLE
                        holder.requesterReview.visibility = View.VISIBLE
                        holder.requesterUpdate.visibility = View.GONE
                        holder.statusesUpdateLayout.visibility = View.GONE

                        if (document.data.get("rating").toString()=="null"){
                            holder.requesterRating.text = "Rating: 1"
                            holder.requesterReview.text = "Review: \n" + "No Review"
                        }
                        else{
                            holder.requesterRating.text = "Rating: " + document.data.get("rating").toString()
                            holder.requesterReview.text = "Review: \n" + document.data.get("review").toString()
                        }
                    }

                    if (document.data.get("status").toString() == "Declined")
                    {
                        holder.requesterUpdate.visibility = View.GONE
                        holder.statusesUpdateLayout.visibility = View.GONE
                        holder.requesterRating.visibility = View.GONE
                        holder.requesterReview.visibility = View.GONE
                    }

                }
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }



        holder.requesterMoreInfo.setOnClickListener {
            try{

            if (holder.requesterMoreDetails.visibility == View.GONE) {
                holder.requesterMoreDetails.visibility = View.VISIBLE
                holder.requesterMoreInfo.text = "Less Info"

                if (requesterJobStatuses[position] == "New Requests") {
                    holder.requesterAccept.visibility = View.VISIBLE
                    holder.requesterDecline.visibility = View.VISIBLE

                    holder.statusesUpdateLayout.visibility = View.GONE
                    holder.requesterUpdate.visibility = View.GONE
                } else {
                    holder.requesterAccept.visibility = View.GONE
                    holder.requesterDecline.visibility = View.GONE

                    holder.statusesUpdateLayout.visibility = View.VISIBLE
                    holder.requesterUpdate.visibility = View.VISIBLE
                }

            } else {
                holder.requesterMoreDetails.visibility = View.GONE
                holder.requesterMoreInfo.text = "More Info"
            }

            }
            catch (e : Exception){
                Log.d("TAG", e.toString())
            }
        }

        holder.requesterAccept.setOnClickListener {
            db.collection("jobcards")
                .whereEqualTo("userId", userIds[position])
                .whereEqualTo("garageId", auth.currentUser?.uid.toString())
                .whereEqualTo("status", "New Requests")
                .whereEqualTo("date", requesterDateTimes[position])
                .get()
                .addOnSuccessListener {
                    for (document in it)
                    {
                        db.collection("jobcards").document(document.id)
                            .update("status", "Pending")
                            .addOnSuccessListener {
                                holder.requesterMoreDetails.visibility = View.GONE

                                sendUpdateNotification(userIds[position], "Pending")

                                filterData("New Requests")
                            }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }

                    }
                }
                .addOnFailureListener {
                    Log.d("TAG", "Failed to get data")
                }
        }

        holder.requesterDecline.setOnClickListener {
            db.collection("jobcards")
                .whereEqualTo("userId", userIds[position])
                .whereEqualTo("garageId", auth.currentUser?.uid.toString())
                .whereEqualTo("status", "New Requests")
                .whereEqualTo("date", requesterDateTimes[position])
                .get()
                .addOnSuccessListener {
                    for (document in it)
                    {
                        db.collection("jobcards").document(document.id)
                            .update("status", "Declined")
                            .addOnSuccessListener {
                                holder.requesterMoreDetails.visibility = View.GONE

                                sendUpdateNotification(userIds[position], "Declined")

                                filterData("New Requests")
                            }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }

                    }
                }
                .addOnFailureListener {
                    Log.d("TAG", "Failed to get data")
                }
        }

        holder.requesterUpdate.setOnClickListener {
            db.collection("jobcards")
                .whereEqualTo("userId", userIds[position])
                .whereEqualTo("garageId", auth.currentUser?.uid.toString())
                .whereEqualTo("status", requesterJobStatuses[position])
                .whereEqualTo("date", requesterDateTimes[position])
                .get()
                .addOnSuccessListener {
                    for (document in it)
                    {
                        if (holder.statusesUpdate.text.toString() in listOf("Pending", "In Progress", "Declined", "Completed")){

                        if (holder.statusesUpdate.text.toString() == "Completed"){
                            val dialogView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.payment_dialog, null)
                            val finalAmount = dialogView.findViewById<EditText>(R.id.finalAmount)


                            val alterDialog = MaterialAlertDialogBuilder(holder.itemView.context)
                                .setTitle("Payment")
                                .setMessage("Enter the Final Amount for this jobcard")
                                .setView(dialogView)
                                .setPositiveButton("Submit", null)
                                .setNegativeButton("Cancel"){
                                        dialog, _ -> dialog.dismiss()
                                }
                                .show()

                            alterDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {

                                if (finalAmount.text.toString().isNotEmpty()){1
                                    if (finalAmount.text.toString().length >= 2 && finalAmount.text.toString().length < 6){
                                        if (finalAmount.text.toString().toInt() >= 0){

                                        db.collection("jobcards").document(document.id)
                                            .update("status", holder.statusesUpdate.text.toString(),
                                                "finalAmount", finalAmount.text.toString().toInt())
                                            .addOnSuccessListener {
                                                holder.requesterMoreDetails.visibility = View.GONE
                                                holder.requesterMoreInfo.text = "More Info"

                                                sendUpdateNotification(userIds[position], holder.statusesUpdate.text.toString())

                                                filterData(requesterJobStatuses[position])

                                                alterDialog.dismiss()
                                            }
                                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                                        }
                                        else{
                                            finalAmount.error = "Invalid Amount"
                                            finalAmount.requestFocus()
                                        }
                                    }
                                    else{
                                        finalAmount.error = "Invalid Amount"
                                        finalAmount.requestFocus()
                                    }
                                }
                            }


                        }
                        else{

                            db.collection("jobcards").document(document.id)
                                .update("status", holder.statusesUpdate.text.toString())
                                .addOnSuccessListener {
                                    holder.requesterMoreDetails.visibility = View.GONE
                                    holder.requesterMoreInfo.text = "More Info"

                                    sendUpdateNotification(userIds[position], holder.statusesUpdate.text.toString())

                                    filterData(requesterJobStatuses[position])
                                }
                                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                            }
                        }
                        else{
                            holder.statusesUpdateLayout.error = "Invalid Status"
                            holder.statusesUpdateLayout.requestFocus()
                        }

                    }
                }
                .addOnFailureListener {
                    Log.d("TAG", "Failed to get data")
                }
        }

    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var requesterMoreInfo : TextView
        lateinit var requesterMoreDetails : LinearLayout

        lateinit var requesterDateTime : TextView
        lateinit var requesterName : TextView
        lateinit var requesterService : TextView
        lateinit var requesterJobStatus : TextView

        lateinit var requesterVehicleName : TextView
        lateinit var requesterVehicleType : TextView
        lateinit var requesterVehicleModel : TextView
        lateinit var requesterVehicleNumber : TextView
        lateinit var requesterFuelType : TextView
        lateinit var requesterTowRequired : TextView

        lateinit var requesterAccept : Button
        lateinit var requesterDecline : Button

        lateinit var statusesUpdateLayout : TextInputLayout
        lateinit var statusesUpdate : AutoCompleteTextView

        lateinit var requesterUpdate : Button

        lateinit var requesterRating : TextView
        lateinit var requesterReview : TextView

        lateinit var requesterCall : ImageView
        lateinit var requesterMaps : ImageView

        init {
            requesterMoreInfo = itemView.findViewById(R.id.requesterMoreInfo)
            requesterMoreDetails = itemView.findViewById(R.id.requesterMoreDetails)
            requesterMoreDetails.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

            val transition = AutoTransition()
            TransitionManager.beginDelayedTransition(requesterMoreDetails, transition)

            requesterDateTime = itemView.findViewById(R.id.requesterDateTime)
            requesterName = itemView.findViewById(R.id.requesterName)
            requesterService = itemView.findViewById(R.id.requesterServiceRequired)
            requesterJobStatus = itemView.findViewById(R.id.requesterJobStatus)

            requesterVehicleName = itemView.findViewById(R.id.requesterVehicleName)
            requesterVehicleType = itemView.findViewById(R.id.requesterVehicleType)
            requesterVehicleModel = itemView.findViewById(R.id.requesterVehicleModel)
            requesterVehicleNumber = itemView.findViewById(R.id.requesterVehicleNumber)
            requesterFuelType = itemView.findViewById(R.id.requesterFuelType)
            requesterTowRequired = itemView.findViewById(R.id.requesterTowRequired)

            requesterAccept = itemView.findViewById(R.id.acceptRequest)
            requesterDecline = itemView.findViewById(R.id.declineRequest)

            requesterUpdate = itemView.findViewById(R.id.updateStatus)

            statusesUpdateLayout = itemView.findViewById(R.id.statusesUpdateLayout)
            statusesUpdate = itemView.findViewById(R.id.statusesUpdate)

            requesterRating = itemView.findViewById(R.id.requesterRating)
            requesterReview = itemView.findViewById(R.id.requesterReview)

            requesterCall = itemView.findViewById(R.id.requesterCall)
            requesterMaps = itemView.findViewById(R.id.requesterMaps)

            var statuses = arrayOf<String>("Pending", "In Progress", "Declined", "Completed")
            val statusesTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                itemView.context,
                android.R.layout.simple_list_item_1,
                statuses
            )
            statusesUpdate.setAdapter(statusesTypeAdapter)
        }

    }

    fun filterData(status : String){

        requesterDateTimes = emptyList()
        requesterNames = emptyList()
        requesterServices  = emptyList()
        requesterJobStatuses = emptyList()

        requesterVehicleNames = emptyList()
        requesterVehicleTypes = emptyList()
        requesterVehicleModels = emptyList()
        requesterVehicleNumbers = emptyList()
        requesterFuelTypes = emptyList()
        requesterTowRequires = emptyList()
        requesterContacts = emptyList()

        userIds = emptyList()

        db.collection("jobcards")
            .whereEqualTo("status", status)
            .whereEqualTo("garageId", auth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    requesterDateTimes = requesterDateTimes + document.data.get("date").toString()
                    requesterNames = requesterNames + document.data?.get("name").toString()
                    requesterServices = requesterServices + document.data?.get("serviceType").toString()
                    requesterJobStatuses = requesterJobStatuses + document.data?.get("status").toString()

                    requesterVehicleNames = requesterVehicleNames + document.data?.get("vehicleName")
                        .toString()
                    requesterVehicleTypes = requesterVehicleTypes + document.data?.get("vehicleType")
                        .toString()
                    requesterVehicleModels = requesterVehicleModels + document.data?.get("vehicleModel")
                        .toString()
                    requesterVehicleNumbers = requesterVehicleNumbers + document.data?.get("vehicleNumber")
                        .toString()
                    requesterFuelTypes = requesterFuelTypes + document.data?.get("vehicleFuelType").toString()
                    requesterTowRequires = requesterTowRequires + document.data?.get("towLocation").toString()
                    requesterContacts = requesterContacts + document.data?.get("contact").toString()

                    userIds = userIds + document.data?.get("userId").toString()

                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }


    }

    fun sendUpdateNotification(userId : String, status : String)
    {
        db.collection("jobcards")
            .whereEqualTo("userId", userId)
            .whereEqualTo("garageId", auth.currentUser?.uid.toString())
            .whereEqualTo("date", requesterDateTimes[0])
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    var garageName = document.data.get("garageName").toString()
                    var serviceType = document.data.get("serviceType").toString()
                    var vehicleName = document.data.get("vehicleName").toString()
                    var vehicleNumber = document.data.get("vehicleNumber").toString()
                    var status = status
                    var notificationType = "jobcardUpdate"

                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener {
                            var token = it.data?.get("token").toString()

                            var jsonObject = JSONObject()
                            var jsonObjectData = JSONObject()

                            jsonObjectData.put("title", "Jobcard Update!")
                            jsonObjectData.put("body", "Your jobcard update for $vehicleNumber by $garageName")
                            jsonObjectData.put("garageName", garageName)
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
                        .addOnFailureListener {
                            Log.d("TAG", "Failed to get data")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
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