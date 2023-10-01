package dev.falcon.garagefinderpro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserRequestsRecyclerAdapter : RecyclerView.Adapter<UserRequestsRecyclerAdapter.ViewHolder>()
{
    var requestedDateTimes = listOf<String>()
    var requestedNames = listOf<String>()
    var requestedServices  = listOf<String>()
    var requestedJobStatuses = listOf<String>()

    var requestedVehicleNames = listOf<String>()
    var requestedVehicleTypes = listOf<String>()
    var requestedVehicleModels = listOf<String>()
    var requestedVehicleNumbers = listOf<String>()
    var requestedFuelTypes = listOf<String>()
    var requestedTowRequires = listOf<String>()

    var garageIds = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("jobcards")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                        requestedDateTimes = requestedDateTimes + document.data.get("date").toString()
                        requestedNames = requestedNames + document.data.get("name").toString()
                        requestedServices = requestedServices + document.data.get("serviceType").toString()
                        requestedJobStatuses = requestedJobStatuses + document.data.get("status").toString()

                        requestedVehicleNames = requestedVehicleNames + document.data.get("vehicleName")
                            .toString()
                        requestedVehicleTypes = requestedVehicleTypes + document.data.get("vehicleType")
                            .toString()
                        requestedVehicleModels = requestedVehicleModels + document.data.get("vehicleModel")
                            .toString()
                        requestedVehicleNumbers = requestedVehicleNumbers + document.data.get("vehicleNumber")
                            .toString()
                        requestedFuelTypes = requestedFuelTypes + document.data.get("vehicleFuelType")
                            .toString()
                        requestedTowRequires = requestedTowRequires + document.data.get("towLocation")
                            .toString()

                        garageIds = garageIds + document.data.get("garageId").toString()

                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
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
        holder.requestedName.text = requestedNames[position]
        holder.requestedService.text = requestedServices[position]
        holder.requestedJobStatus.text = requestedJobStatuses[position]

        holder.requestedVehicleName.text = requestedVehicleNames[position]
        holder.requestedVehicleType.text = requestedVehicleTypes[position]
        holder.requestedVehicleModel.text = requestedVehicleModels[position]
        holder.requestedVehicleNumber.text = requestedVehicleNumbers[position]
        holder.requestedFuelType.text = requestedFuelTypes[position]
        holder.requestedTowRequired.text = requestedTowRequires[position]

        holder.requesterMoreInfo.setOnClickListener {
            if (holder.requesterMoreDetails.visibility == View.GONE)
            {
                holder.requesterMoreDetails.visibility = View.VISIBLE
            }
            else
            {
                holder.requesterMoreDetails.visibility = View.GONE
            }
        }

        holder.requestedDecline.setOnClickListener {
            db.collection("jobcards")
                .whereEqualTo("garageId", garageIds[position])
                .whereEqualTo("date", requestedDateTimes[position])
                .get()
                .addOnSuccessListener {
                    for (document in it)
                    {
                        db.collection("jobcards")
                            .document(document.id)
                            .update("status", "Cancelled")
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
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

        lateinit var requestedDateTime : TextView
        lateinit var requestedName : TextView
        lateinit var requestedService : TextView
        lateinit var requestedJobStatus : TextView

        lateinit var requestedVehicleName : TextView
        lateinit var requestedVehicleType : TextView
        lateinit var requestedVehicleModel : TextView
        lateinit var requestedVehicleNumber : TextView
        lateinit var requestedFuelType : TextView
        lateinit var requestedTowRequired : TextView

        lateinit var requestedDecline : Button

        lateinit var requestedStatus : AutoCompleteTextView

        init {
            requesterMoreInfo = itemView.findViewById(R.id.requesterMoreInfo)
            requesterMoreDetails = itemView.findViewById(R.id.requesterMoreDetails)

            requestedDateTime = itemView.findViewById(R.id.requestedDateTime)
            requestedName = itemView.findViewById(R.id.requestedName)
            requestedService = itemView.findViewById(R.id.requestedServiceRequired)
            requestedJobStatus = itemView.findViewById(R.id.requestedJobStatus)

            requestedVehicleName = itemView.findViewById(R.id.requestedVehicleName)
            requestedVehicleType = itemView.findViewById(R.id.requestedVehicleType)
            requestedVehicleModel = itemView.findViewById(R.id.requestedVehicleModel)
            requestedVehicleNumber = itemView.findViewById(R.id.requestedVehicleNumber)
            requestedFuelType = itemView.findViewById(R.id.requestedFuelType)
            requestedTowRequired = itemView.findViewById(R.id.requestedTowRequired)

            requestedDecline = itemView.findViewById(R.id.cancelRequest)

            requestedStatus = itemView.findViewById(R.id.requestedStatus)

            var statuses = arrayOf<String>("Pending", "In Progress", "Declined", "Completed", "Cancelled")
            val statusesTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                itemView.context,
                android.R.layout.simple_list_item_1,
                statuses
            )
            requestedStatus.setAdapter(statusesTypeAdapter)

        }
    }

    fun filterRequestedData(status: String){
        requestedDateTimes = listOf<String>()
        requestedNames = listOf<String>()
        requestedServices  = listOf<String>()
        requestedJobStatuses = listOf<String>()

        requestedVehicleNames = listOf<String>()
        requestedVehicleTypes = listOf<String>()
        requestedVehicleModels = listOf<String>()
        requestedVehicleNumbers = listOf<String>()
        requestedFuelTypes = listOf<String>()
        requestedTowRequires = listOf<String>()

        garageIds = listOf<String>()

        db.collection("jobcards")
            .get()
            .addOnSuccessListener {
                for (document in it)
                {
                    if (document.data.get("status").toString() == status)
                    {
                        requestedDateTimes = requestedDateTimes + document.data.get("date").toString()
                        requestedNames = requestedNames + document.data.get("name").toString()
                        requestedServices = requestedServices + document.data.get("serviceType").toString()
                        requestedJobStatuses = requestedJobStatuses + document.data.get("status").toString()

                        requestedVehicleNames = requestedVehicleNames + document.data.get("vehicleName")
                            .toString()
                        requestedVehicleTypes = requestedVehicleTypes + document.data.get("vehicleType")
                            .toString()
                        requestedVehicleModels = requestedVehicleModels + document.data.get("vehicleModel")
                            .toString()
                        requestedVehicleNumbers = requestedVehicleNumbers + document.data.get("vehicleNumber")
                            .toString()
                        requestedFuelTypes = requestedFuelTypes + document.data.get("vehicleFuelType")
                            .toString()
                        requestedTowRequires = requestedTowRequires + document.data.get("towLocation")
                            .toString()

                        garageIds = garageIds + document.data.get("garageId").toString()
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failed to get data")
            }
    }


}