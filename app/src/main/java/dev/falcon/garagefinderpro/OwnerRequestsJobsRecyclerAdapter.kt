package dev.falcon.garagefinderpro

import android.animation.LayoutTransition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
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

    var userIds = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("jobcards")
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    if (document.data.get("status").toString() == "New Requests")
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
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }
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

        if (requesterJobStatuses[position] == "New Requests")
        {
            holder.requesterAccept.visibility = View.VISIBLE
            holder.requesterDecline.visibility = View.VISIBLE

            holder.statusesUpdateLayout.visibility = View.GONE
            holder.requesterUpdate.visibility = View.GONE
        }
        else
        {
            holder.requesterAccept.visibility = View.GONE
            holder.requesterDecline.visibility = View.GONE

            holder.statusesUpdateLayout.visibility = View.VISIBLE
            holder.requesterUpdate.visibility = View.VISIBLE
        }

        holder.requesterMoreInfo.setOnClickListener {


            if (holder.requesterMoreDetails.visibility == View.GONE) {
                holder.requesterMoreDetails.visibility = View.VISIBLE
                holder.requesterMoreInfo.text = "Less Info"
            } else {
                holder.requesterMoreDetails.visibility = View.GONE
                holder.requesterMoreInfo.text = "More Info"
            }

        }

        holder.requesterAccept.setOnClickListener {
            db.collection("jobcards").whereEqualTo("userId", userIds[position])
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
            db.collection("jobcards").whereEqualTo("userId", userIds[position])
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
            db.collection("jobcards").whereEqualTo("userId", userIds[position])
                .whereEqualTo("status", requesterJobStatuses[position])
                .whereEqualTo("date", requesterDateTimes[position])
                .get()
                .addOnSuccessListener {
                    for (document in it)
                    {
                        if (holder.statusesUpdate.text.toString() in listOf("Pending", "In Progress", "Declined", "Completed")){


                        db.collection("jobcards").document(document.id)
                            .update("status", holder.statusesUpdate.text.toString())
                            .addOnSuccessListener {
                                holder.requesterMoreDetails.visibility = View.GONE

                                sendUpdateNotification(userIds[position], holder.statusesUpdate.text.toString())

                                filterData(requesterJobStatuses[position])
                            }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
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

            var statuses = arrayOf<String>("Pending", "In Progress", "Declined", "Completed", "Cancelled")
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

        userIds = emptyList()

        db.collection("jobcards")
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    if (document.data.get("status").toString() == status)
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

                        userIds = userIds + document.data?.get("userId").toString()
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }


    }

    fun sendUpdateNotification(userId : String, status : String)
    {
        db.collection("jobcards").whereEqualTo("userId", userId).whereEqualTo("date", requesterDateTimes[0])
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