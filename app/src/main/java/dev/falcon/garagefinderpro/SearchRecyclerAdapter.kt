package dev.falcon.garagefinderpro

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject

class SearchRecyclerAdapter : RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>()
{
    var garageNames = listOf<String>()
    var garageAddresses = listOf<String>()
    var garageTimings = listOf<String>()
    var tokens = listOf<String>()
    var garageMinCost = listOf<String>()
    var garageTowing = listOf<String>()
    var garageContact1 = listOf<String>()
    var garageContact2 = listOf<String>()
    var garageRatings = listOf<String>()
    var garageStatus = listOf<String>()
    var garagePhoto = listOf<String>()
    var garageCover = listOf<String>()
    var garageSpecialization = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    lateinit var moreInfoGarage : Button
    lateinit var requestService : Button

    lateinit var detailsGarageName : TextView
    lateinit var detailsOwnerImage : ImageView
    lateinit var detailsCoverImage : ImageView
    lateinit var detailsGarageTiming : TextView
    lateinit var detailsGarageTowing : TextView
    lateinit var detailsGarageMinCost : TextView
    lateinit var detailsGarageStatus : TextView

    lateinit var detailsGarageCall1 : Button
    lateinit var detailsGarageCall2 : Button

    lateinit var detailsSpecialization1 : TextView
    lateinit var detailsSpecialization2 : TextView
    lateinit var detailsSpecialization3 : TextView
    lateinit var detailsSpecialization4 : TextView
    lateinit var detailsSpecialization5 : TextView

    lateinit var serviceTypeTxt : AutoCompleteTextView
    lateinit var vehicleWhichTxt : AutoCompleteTextView
    lateinit var towRequired : CheckBox
    lateinit var requestServiceSubmit : Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if(document.data["type"].toString() == "garageowner")
                    {
                        garageNames = garageNames + document.data["name"].toString()
                        garageAddresses = garageAddresses + document.data["garageAddress"].toString()
                        garageTimings = garageTimings + ("Timing: "+document.data["garageTime"].toString())
                        tokens = tokens + document.data["token"].toString()
                        garageMinCost = garageMinCost + ("Min. Service Cost: "+document.data["garageServiceCost"].toString())
                        garageTowing = garageTowing + document.data["garageTowing"].toString()
                        garageContact1 = garageContact1 + document.data["garageContact1"].toString()
                        garageContact2 = garageContact2 + document.data["garageContact2"].toString()
//                    garageRatings = garageRatings + document.data["garageRatings"].toString()
                        garageStatus = garageStatus + document.data["garageStatus"].toString()
                        garagePhoto = garagePhoto + document.data["photo"].toString()
                        garageCover = garageCover + document.data["cover"].toString()
                        garageSpecialization = garageSpecialization + document.data["garageSpecialization"].toString()
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "onCreateView: ${it.message}")
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.search_resource, parent, false)

        moreInfoGarage = v.findViewById(R.id.moreInfoGarage)

        requestService = v.findViewById(R.id.requestService)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return garageNames.size
    }

    override fun onBindViewHolder(holder: SearchRecyclerAdapter.ViewHolder, position: Int) {
        holder.garageName.text = garageNames[position]
        holder.garageAddress.text = garageAddresses[position]
        holder.garageTiming.text = garageTimings[position]
        holder.garageMinCost.text = garageMinCost[position]
//        holder.garageRating.text = garageRatings[position]
        Picasso.get().load(garagePhoto[position].toUri()).into(holder.garagePhoto)
        holder.garageStatus.text = garageStatus[position]

        var specialization = garageSpecialization[position].split(",")

        holder.garageSpecialization1.text = specialization[0]
        holder.garageSpecialization2.text = specialization[1]
        holder.garageSpecialization3.text = specialization[2]
        holder.garageSpecialization4.text = specialization[3]
        holder.garageSpecialization5.text = specialization[4]

        var bottomSheetDialog = BottomSheetDialog(holder.itemView.context)
        val bottomSheetView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.moredetails_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        moreInfoGarage.setOnClickListener {
            bottomSheetDialog.show()

            detailsGarageName = bottomSheetView.findViewById(R.id.detailsGarageName)
            detailsOwnerImage = bottomSheetView.findViewById(R.id.detailsOwnerImage)
            detailsCoverImage = bottomSheetView.findViewById(R.id.detailsCoverImage)
            detailsGarageTiming = bottomSheetView.findViewById(R.id.detailsGarageTiming)
            detailsGarageTowing = bottomSheetView.findViewById(R.id.detailsGarageTowing)
            detailsGarageMinCost = bottomSheetView.findViewById(R.id.detailsGarageMinCost)
            detailsGarageStatus = bottomSheetView.findViewById(R.id.detailsGarageStatus)

            detailsGarageCall1 = bottomSheetView.findViewById(R.id.detailsGarageCall1)
            detailsGarageCall2 = bottomSheetView.findViewById(R.id.detailsGarageCall2)

            detailsSpecialization1 = bottomSheetView.findViewById(R.id.detailsSpecialization1)
            detailsSpecialization2 = bottomSheetView.findViewById(R.id.detailsSpecialization2)
            detailsSpecialization3 = bottomSheetView.findViewById(R.id.detailsSpecialization3)
            detailsSpecialization4 = bottomSheetView.findViewById(R.id.detailsSpecialization4)
            detailsSpecialization5 = bottomSheetView.findViewById(R.id.detailsSpecialization5)

            detailsGarageName.text = garageNames[position]
            Picasso.get().load(garagePhoto[position].toUri()).placeholder(R.drawable.blank).into(detailsOwnerImage)
            Picasso.get().load(garageCover[position].toUri()).placeholder(R.drawable.blank).into(detailsCoverImage)
            detailsGarageTiming.text = garageTimings[position]
            detailsGarageTowing.text = "Towing Serivce: " + garageTowing[position]
            detailsGarageMinCost.text = garageMinCost[position]
            detailsGarageStatus.text = "Status: " + garageStatus[position]

            detailsGarageCall1.setOnClickListener {
                if (garageContact1[position] != null && garageContact1[position].isNotEmpty())
                {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    intent.data = android.net.Uri.parse("tel:" + garageContact1[position])
                    holder.itemView.context.startActivity(intent)
                }
                else{
                    detailsGarageCall1.isEnabled = false
                    detailsGarageCall2.setBackgroundColor(R.color.grey2)
                    Toast.makeText(holder.itemView.context, "No Contact Found", Toast.LENGTH_SHORT).show()
                }
            }
            detailsGarageCall2.setOnClickListener {
                if (garageContact2[position]!= null && garageContact2[position].isNotEmpty()){
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    intent.data = android.net.Uri.parse("tel:" + garageContact2[position])
                    holder.itemView.context.startActivity(intent)
                }
                else{
                    detailsGarageCall2.isEnabled = false
                    detailsGarageCall2.setBackgroundColor(R.color.grey2)
                    Toast.makeText(holder.itemView.context, "No Contact Found", Toast.LENGTH_SHORT).show()
                }
            }


            var specialization = garageSpecialization[position].split(",")

            detailsSpecialization1.text = "◆  " + specialization[0]
            detailsSpecialization2.text = "◆  " + specialization[1]
            detailsSpecialization3.text = "◆  " + specialization[2]
            detailsSpecialization4.text = "◆  " + specialization[3]
            detailsSpecialization5.text = "◆  " + specialization[4]

            var getGoogleMaps : Button = bottomSheetView.findViewById(R.id.getGoogleMaps)

            getGoogleMaps.setOnClickListener {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("geo:0,0?q=" + garageAddresses[position])
                holder.itemView.context.startActivity(intent)
            }

        }


        var bottomSheetDialog2 = BottomSheetDialog(holder.itemView.context)
        val bottomSheetView2 = LayoutInflater.from(holder.itemView.context).inflate(R.layout.requestservice_bottomsheet, null)
        bottomSheetDialog2.setContentView(bottomSheetView2)

        requestService.setOnClickListener {
            bottomSheetDialog2.show()

            serviceTypeTxt = bottomSheetView2.findViewById(R.id.serviceType)
            val serviceTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                holder.itemView.context,
                android.R.layout.simple_list_item_1,
                holder.itemView.context.resources.getStringArray(R.array.serviceTypes)
            )
            serviceTypeTxt.setAdapter(serviceTypeAdapter)

            db.collection("users").document(auth.currentUser!!.uid).collection("vehicles")
                .get()
                .addOnSuccessListener { documents ->
                    var vehicleWhich = listOf<String>()
                    for (document in documents) {
                        vehicleWhich = vehicleWhich + document.data["vehicleNumber"].toString()
                    }
                    vehicleWhichTxt = bottomSheetView2.findViewById(R.id.vehicleWhich)
                    val vehicleWhichAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        holder.itemView.context,
                        android.R.layout.simple_list_item_1,
                        vehicleWhich
                    )
                    vehicleWhichTxt.setAdapter(vehicleWhichAdapter)
                }
                .addOnFailureListener {
                    Log.d("TAG", "onCreateView: ${it.message}")
                }

            towRequired = bottomSheetView2.findViewById(R.id.towRequired)

            requestServiceSubmit = bottomSheetView2.findViewById(R.id.requestServiceSubmit)

            var towLocation : String = ""


            fusedLocationClient = LocationServices.getFusedLocationProviderClient(holder.itemView.context)

            requestServiceSubmit.setOnClickListener {
                if (serviceTypeTxt.text.toString().isNotEmpty() && vehicleWhichTxt.text.toString().isNotEmpty() ) {
                    if (serviceTypeTxt.text.toString() in holder.itemView.context.resources.getStringArray(
                            R.array.serviceTypes
                        )
                    ) {
                        if (vehicleWhichTxt.text.toString() != "Select Your Vehicle") {
                            if (garageStatus[position] == "Open") {
                                bottomSheetDialog2.dismiss()

                                if (towRequired.isChecked) {
                                    if (ContextCompat.checkSelfPermission(
                                            holder.itemView.context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val locationManager =
                                            holder.itemView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                            val locationRequest = LocationRequest.create()
                                            locationRequest.priority =
                                                LocationRequest.PRIORITY_HIGH_ACCURACY
                                            locationRequest.interval = 30 * 1000
                                            locationRequest.fastestInterval = 5 * 1000

                                            val builder =
                                                LocationSettingsRequest.Builder()
                                                    .addLocationRequest(locationRequest)

                                            builder.setAlwaysShow(true)

                                            val result =
                                                LocationServices.getSettingsClient(holder.itemView.context)
                                                    .checkLocationSettings(builder.build())

                                            result.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                                        if (location != null) {
                                                            val geocoder =
                                                                Geocoder(holder.itemView.context)
                                                            val addresses =
                                                                geocoder.getFromLocation(
                                                                    location.latitude,
                                                                    location.longitude,
                                                                    1
                                                                )

                                                            towLocation =
                                                                addresses!![0]?.getAddressLine(0)
                                                                    .toString()

                                                        } else {
                                                            Toast.makeText(
                                                                holder.itemView.context,
                                                                "Location not found",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                        .addOnFailureListener {
                                                            Toast.makeText(
                                                                holder.itemView.context,
                                                                "Location not found",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                } else {
                                                    if (task.exception is ResolvableApiException) {
                                                        try {
                                                            val resolvable =
                                                                task.exception as ResolvableApiException
                                                            resolvable.startResolutionForResult(
                                                                holder.itemView.context as Activity,
                                                                REQUEST_ENABLE_LOCATION
                                                            )
                                                        } catch (sendEx: IntentSender.SendIntentException) {
                                                            Log.d(
                                                                "TAG",
                                                                "Error starting resolution for location settings"
                                                            )
                                                        }
                                                    } else {
                                                        Log.d(
                                                            "TAG",
                                                            "getLocation: " + task.exception
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                                if (location != null) {
                                                    val geocoder = Geocoder(holder.itemView.context)
                                                    val addresses =
                                                        geocoder.getFromLocation(
                                                            location.latitude,
                                                            location.longitude,
                                                            1
                                                        )

                                                    towLocation =
                                                        addresses!![0]?.getAddressLine(0).toString()

                                                } else {
                                                    Toast.makeText(
                                                        holder.itemView.context,
                                                        "Location not found",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                                .addOnFailureListener {
                                                    Toast.makeText(
                                                        holder.itemView.context,
                                                        "Location not found",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        ActivityCompat.requestPermissions(
                                            holder.itemView.context as Activity,
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                            1
                                        )
                                    }
                                } else {
                                    towLocation = "Not Required"
                                }

                                db.collection("users").document(auth.currentUser!!.uid)
                                    .collection("vehicles")
                                    .get()
                                    .addOnSuccessListener { documents ->
                                        var vehicleName = ""
                                        var vehicleFuelType = ""
                                        var vehicleType = ""
                                        var vehicleModel = ""
                                        for (document in documents) {
                                            if (document.data["vehicleNumber"].toString() == vehicleWhichTxt.text.toString()) {
                                                vehicleName =
                                                    document.data["vehicleName"].toString()
                                                vehicleFuelType =
                                                    document.data["fuelType"].toString()
                                                vehicleType =
                                                    document.data["vehicleType"].toString()
                                                vehicleModel =
                                                    document.data["vehicleModel"].toString()
                                            }
                                        }
                                        sendRequestNotification(
                                            auth.currentUser!!.displayName.toString(),
                                            tokens[position],
                                            serviceTypeTxt.text.toString(),
                                            vehicleName,
                                            vehicleFuelType,
                                            vehicleType,
                                            vehicleModel,
                                            towLocation
                                        )
                                    }
                                    .addOnFailureListener {
                                        Log.d("TAG", "onCreateView: ${it.message}")
                                    }
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Garage is Closed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            vehicleWhichTxt.error = "Please select from the list"
                            vehicleWhichTxt.requestFocus()
                        }
                    } else {
                        serviceTypeTxt.error = "Please select from the list"
                        serviceTypeTxt.requestFocus()
                    }
                }
                else{
                    Toast.makeText(holder.itemView.context, "Please Fill All the Details!", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var garageName : TextView
        lateinit var garageAddress : TextView
        lateinit var garageTiming : TextView
        lateinit var garageMinCost : TextView
        lateinit var garageRating : TextView
        lateinit var garageStatus : TextView
        lateinit var garagePhoto : ImageView

        lateinit var garageSpecialization1 : TextView
        lateinit var garageSpecialization2 : TextView
        lateinit var garageSpecialization3 : TextView
        lateinit var garageSpecialization4 : TextView
        lateinit var garageSpecialization5 : TextView


        init {
            garageName = itemView.findViewById(R.id.garageName)
            garageAddress = itemView.findViewById(R.id.garageAddress)
            garageTiming = itemView.findViewById(R.id.garageTiming)
            garageMinCost = itemView.findViewById(R.id.garageMinCost)
            garageRating = itemView.findViewById(R.id.garageRating)
            garageStatus = itemView.findViewById(R.id.garageStatus)
            garagePhoto = itemView.findViewById(R.id.garageImage)

            garageSpecialization1 = itemView.findViewById(R.id.garageSpecialization1)
            garageSpecialization2 = itemView.findViewById(R.id.garageSpecialization2)
            garageSpecialization3 = itemView.findViewById(R.id.garageSpecialization3)
            garageSpecialization4 = itemView.findViewById(R.id.garageSpecialization4)
            garageSpecialization5 = itemView.findViewById(R.id.garageSpecialization5)
        }
    }

    fun sendRequestNotification(name: String, token: String, serviceType: String, vehicleName: String, vehicleFuelType: String, vehicleType: String, vehicleModel: String, towLocation: String)
    {
        var jsonObject = JSONObject()
        var jsonObjectData = JSONObject()

        jsonObjectData.put("title", "New Service Request!")
        jsonObjectData.put("body", "You have a new service request from $name")
        jsonObjectData.put("serviceType", serviceType)
        jsonObjectData.put("vehicleName", vehicleName)
        jsonObjectData.put("vehicleFuelType", vehicleFuelType)
        jsonObjectData.put("vehicleType", vehicleType)
        jsonObjectData.put("vehicleModel", vehicleModel)
        jsonObjectData.put("towLocation", towLocation)
        jsonObjectData.put("notificationType", "sendRequest")

        Log.d("TAG", "sendRequestNotification: $jsonObjectData")

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

    fun processNotification(jsonObject: JSONObject){
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

        Log.d("TAG", "processNotification: ${response.body!!.string()}")
    }
}