package dev.falcon.garagefinderpro

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.squareup.picasso.Picasso
import java.util.*


class OwnerProfileActivity : Fragment() {

    private val db = Firebase.firestore
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    lateinit var ownerName: TextView
    lateinit var ownerImage: ImageView
    lateinit var changeOwnerImage: ImageView
    lateinit var coverImage : ImageView
    lateinit var changeCoverImage: ImageView

    lateinit var garageAddressTxt : TextView
    lateinit var garageTimingsTxt : TextView
    lateinit var garageContactTxt : TextView
    lateinit var garageTowingTxt : TextView
    lateinit var garageMinCostTxt : TextView

    lateinit var updateGarageLocation : ImageView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var bottomSheetDialog2 : BottomSheetDialog
    lateinit var editGarage: Button

    lateinit var editGarageName: TextView
    lateinit var editGarageContact1: TextView
    lateinit var editGarageContact2: TextView
    lateinit var editGarageOpenTime: AutoCompleteTextView
    lateinit var editGarageCloseTime: AutoCompleteTextView
    lateinit var editGarageTowing: AutoCompleteTextView
    lateinit var editGarageServiceCost : AutoCompleteTextView

    lateinit var editShowcaseImages : ImageView
    lateinit var editGarageSpecialization : ImageView

    lateinit var garageSpecialization1 : AutoCompleteTextView
    lateinit var garageSpecialization2 : AutoCompleteTextView
    lateinit var garageSpecialization3 : AutoCompleteTextView
    lateinit var garageSpecialization4 : AutoCompleteTextView
    lateinit var garageSpecialization5 : AutoCompleteTextView

    lateinit var specialization1 : TextView
    lateinit var specialization2 : TextView
    lateinit var specialization3 : TextView
    lateinit var specialization4 : TextView
    lateinit var specialization5 : TextView

    lateinit var bannerscroll1 : ImageView
    lateinit var bannerscroll2 : ImageView
    lateinit var bannerscroll3 : ImageView
    lateinit var bannerscroll4 : ImageView
    lateinit var bannerscroll5 : ImageView
    lateinit var bannerscroll6 : ImageView
    lateinit var bannerscroll7 : ImageView

    lateinit var garageRatings : RatingBar

    lateinit var updateGarageSpecialization : Button

    lateinit var updateGarageDetails : Button

    lateinit var deleteShowcaseImages : ImageView

    var whichImage : String = "null"

    val REQUESTS_CODE = 666

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<DetailsReviewRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.ownerprofile_activity, container, false)

        val auth = FirebaseAuth.getInstance()

        ownerName = view.findViewById(R.id.ownerName)
        ownerName.isSelected = true
        ownerImage = view.findViewById(R.id.ownerImage)
        changeOwnerImage = view.findViewById(R.id.changeOwnerImage)
        coverImage = view.findViewById(R.id.coverImage)
        changeCoverImage = view.findViewById(R.id.changeCoverImage)

        garageAddressTxt = view.findViewById(R.id.garageAddressTxt)
        garageAddressTxt.isSelected = true
        garageTimingsTxt = view.findViewById(R.id.garageTimingsTxt)

        garageContactTxt = view.findViewById(R.id.garageContactTxt)
        garageTowingTxt = view.findViewById(R.id.garageTowingTxt)
        garageMinCostTxt = view.findViewById(R.id.garageMinCostTxt)

        bannerscroll1 = view.findViewById(R.id.bannerscroll1)
        bannerscroll2 = view.findViewById(R.id.bannerscroll2)
        bannerscroll3 = view.findViewById(R.id.bannerscroll3)
        bannerscroll4 = view.findViewById(R.id.bannerscroll4)
        bannerscroll5 = view.findViewById(R.id.bannerscroll5)
        bannerscroll6 = view.findViewById(R.id.bannerscroll6)
        bannerscroll7 = view.findViewById(R.id.bannerscroll7)

        specialization1 = view.findViewById(R.id.specialization1)
        specialization2 = view.findViewById(R.id.specialization2)
        specialization3 = view.findViewById(R.id.specialization3)
        specialization4 = view.findViewById(R.id.specialization4)
        specialization5 = view.findViewById(R.id.specialization5)

        garageRatings = view.findViewById(R.id.ratingBar)

        recyclerview = view.findViewById(R.id.detailsReviewsRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = DetailsReviewRecyclerAdapter(uid.toString())

        recyclerview.adapter = adapter

        val handler = Looper.getMainLooper().let { Handler(it) }

        val balloon: Balloon = createBalloon(requireContext()) {
            setArrowSize(10)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setCornerRadius(4f)
            setPadding(10)
            setAutoDismissDuration(1500L)
            setMargin(10)
            setCornerRadius(6f)
            setText("Click Here to Update Garage Location!")
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.black)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        storageReference = FirebaseStorage.getInstance().reference

        db.collection("users").document(auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val ownerNameText = document.getString("name")
                    ownerName.text = ownerNameText

                    val ownerImageUri = document.getString("photo")
                    if (ownerImageUri.isNullOrEmpty() || ownerImageUri == "null") {
                        ownerImage.setImageResource(R.drawable.blank)
                    } else {
                        Picasso.get()
                            .load(ownerImageUri.toUri())
                            .placeholder(R.drawable.blank)
                            .into(ownerImage)
                    }

                    val coverImageUri = document.getString("cover")
                    if (coverImageUri.isNullOrEmpty() || coverImageUri == "null") {
                        coverImage.setImageResource(R.drawable.blank)
                    } else {
                        Picasso.get()
                            .load(coverImageUri.toUri())
                            .placeholder(R.drawable.blank)
                            .into(coverImage)
                    }

                    val garageAddress = document.getString("garageAddress")
                    garageAddressTxt.text = garageAddress

                    val garageTimings = document.getString("garageTime")
                    garageTimingsTxt.text = garageTimings

                    val garageContact1 = document.getString("garageContact1")
                    val garageContact2 = document.getString("garageContact2")
                    garageContactTxt.text = "Contact: " + garageContact1 + "/" + garageContact2

                    val garageTowing = document.getString("garageTowing")
                    garageTowingTxt.text = "Towing Service: " + garageTowing

                    val garageServiceCost = document.getString("garageServiceCost")
                    garageMinCostTxt.text = "Min. Service Cost: " + garageServiceCost

                    val garageShowcaseImages = document.get("showcaseImages") as List<String>?

                    if (garageShowcaseImages.isNullOrEmpty()){
                        bannerscroll1.setImageResource(R.drawable.blank)
                        bannerscroll2.setImageResource(R.drawable.blank)
                        bannerscroll3.setImageResource(R.drawable.blank)
                        bannerscroll4.setImageResource(R.drawable.blank)
                        bannerscroll5.setImageResource(R.drawable.blank)
                        bannerscroll6.setImageResource(R.drawable.blank)
                        bannerscroll7.setImageResource(R.drawable.blank)
                    }else{

                        val imageViews = listOf(bannerscroll1, bannerscroll2, bannerscroll3, bannerscroll4, bannerscroll5, bannerscroll6, bannerscroll7)

                        for (i in 0 until imageViews.size) {
                            if (i < garageShowcaseImages.size) {
                                Picasso.get()
                                    .load(garageShowcaseImages[i].toUri())
                                    .placeholder(R.drawable.blank)
                                    .into(imageViews[i])
                            } else {
                                imageViews[i].setImageResource(R.drawable.blank)
                            }
                        }


                    }

                    val garageSpecialization = document.getString("garageSpecialization")
                    if (garageSpecialization.isNullOrEmpty()) {
                        specialization1.text = "◆  Specialization 1"
                        specialization2.text = "◆  Specialization 2"
                        specialization3.text = "◆  Specialization 3"
                        specialization4.text = "◆  Specialization 4"
                        specialization5.text = "◆  Specialization 5"
                    } else {
                        val garageSpecializationArray = garageSpecialization?.split(", ")
                        specialization1.text = "◆  " + garageSpecializationArray?.get(0)
                        specialization2.text = "◆  " + garageSpecializationArray?.get(1)
                        specialization3.text = "◆  " + garageSpecializationArray?.get(2)
                        specialization4.text = "◆  " + garageSpecializationArray?.get(3)
                        specialization5.text = "◆  " + garageSpecializationArray?.get(4)
                    }

                    val garageRatingsText = document.getField<Float>("rating").toString()
                    if (garageRatingsText.isNullOrEmpty()){
                        garageRatings.rating = 1.0F
                    }else{
                        if (garageRatingsText == "null"){
                            garageRatings.rating = 1.0F
                        }
                        else{
                            garageRatings.rating = garageRatingsText.substring(0,3).toFloat()
                        }
                    }

                } else {
                    Toast.makeText(context, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("OwnerProfile", "Error getting documents: $exception")
            }


        changeOwnerImage.setOnClickListener {
            imagePicker()
            whichImage = "owner"
        }

        changeCoverImage.setOnClickListener {
            imagePicker()
            whichImage = "cover"
        }

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.editgarage_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        editGarage = view.findViewById(R.id.editGarage)

        editGarage.setOnClickListener {
            bottomSheetDialog.show()

            editGarageName = bottomSheetView.findViewById(R.id.garageName)
            editGarageContact1 = bottomSheetView.findViewById(R.id.garageContact1)
            editGarageContact2 = bottomSheetView.findViewById(R.id.garageContact2)
            editGarageOpenTime = bottomSheetView.findViewById(R.id.garageOpenTime)
            editGarageCloseTime = bottomSheetView.findViewById(R.id.garageCloseTime)
            editGarageTowing = bottomSheetView.findViewById(R.id.garageTowingService)
            editGarageServiceCost = bottomSheetView.findViewById(R.id.garageMinServiceCharge)

            val garageTimesAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.garagetimes)
            )
            editGarageOpenTime.setAdapter(garageTimesAdapter)
            editGarageCloseTime.setAdapter(garageTimesAdapter)

            val garageTowingAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.garagetows)
            )
            editGarageTowing.setAdapter(garageTowingAdapter)

            val garageChargeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.garagecharges)
            )
            editGarageServiceCost.setAdapter(garageChargeAdapter)

            if (uid != null) {
                db.collection("users").document(uid)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Toast.makeText(
                                context,
                                "Error while fetching data!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val garageName = value?.getString("name")
                            editGarageName.text = garageName

                            val garageAddress = value?.getString("garageContact1")
                            editGarageContact1.text = garageAddress

                            val garageContact = value?.getString("garageContact2")
                            editGarageContact2.text = garageContact

                            editGarageOpenTime.hint = "Open Time"
                            editGarageCloseTime.hint = "Close Time"

                            editGarageTowing.hint = "Towing Service"
                            editGarageServiceCost.hint = "Min. Service Cost"

                        }
                    }
            } else {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            }

            updateGarageDetails = bottomSheetView.findViewById(R.id.updateGarageDetails)

            updateGarageDetails.setOnClickListener {
                val garageName = editGarageName.text.toString()
                val garageContact1 = editGarageContact1.text.toString()
                val garageContact2 = editGarageContact2.text.toString()
                val garageOpenTime = editGarageOpenTime.text.toString()
                val garageCloseTime = editGarageCloseTime.text.toString()
                val garageTowing = editGarageTowing.text.toString()
                val garageServiceCost = editGarageServiceCost.text.toString()

                if (garageName.isNotEmpty() || garageContact1.isNotEmpty() || garageOpenTime.isNotEmpty() || garageCloseTime.isNotEmpty()) {
                    if (garageName.matches("^[a-zA-Z]+[a-zA-Z0-9\\s]*[a-zA-Z0-9]\$".toRegex())) {
                        if (garageContact1.matches("^[6789]\\d{9}\$".toRegex())) {
                            if (garageContact2.matches("^[6789]\\d{9}\$".toRegex()) && garageContact2 != garageContact1 || garageContact2.isEmpty()) {
                                if (garageOpenTime in resources.getStringArray(R.array.garagetimes)) {
                                    if (garageCloseTime in resources.getStringArray(R.array.garagetimes)) {
                                        if (garageOpenTime < garageCloseTime) {
                                            if (garageTowing in resources.getStringArray(R.array.garagetows)) {
                                                if (garageServiceCost in resources.getStringArray(R.array.garagecharges)) {
                                                    if (uid != null) {
                                                        db.collection("users").document(uid)
                                                            .update(
                                                                "name",
                                                                garageName.capitalize(),
                                                                "garageContact1",
                                                                garageContact1,
                                                                "garageContact2",
                                                                garageContact2,
                                                                "garageTime",
                                                                garageOpenTime + " - " + garageCloseTime,
                                                                "garageTowing",
                                                                garageTowing,
                                                                "garageServiceCost",
                                                                garageServiceCost
                                                            )
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Garage Details Updated",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                db.collection("users").document(uid)
                                                                    .addSnapshotListener { value, error ->
                                                                        if (error != null) {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Error while fetching data!",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        } else {
                                                                            val garageName =
                                                                                value?.getString("name")
                                                                            ownerName.text = garageName

                                                                            val garageTimings =
                                                                                value?.getString("garageTime")
                                                                            garageTimingsTxt.text =
                                                                                garageTimings

                                                                            val garageContact1 =
                                                                                value?.getString("garageContact1")
                                                                            val garageContact2 =
                                                                                value?.getString("garageContact2")
                                                                            garageContactTxt.text =
                                                                                "Contact: " + garageContact1 + "/" + garageContact2

                                                                            val garageTowing =
                                                                                value?.getString("garageTowing")
                                                                            garageTowingTxt.text =
                                                                                "Towing Service: " + garageTowing

                                                                            val garageServiceCost =
                                                                                value?.getString("garageServiceCost")
                                                                            garageMinCostTxt.text =
                                                                                "Min. Service Cost: " + garageServiceCost

                                                                        }
                                                                    }

                                                                bottomSheetDialog.dismiss()
                                                            }
                                                            .addOnFailureListener {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Garage Details Update Failed",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "User not authenticated",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            } else {
                                                editGarageTowing.error = "Invalid Garage Towing Service"
                                                editGarageTowing.requestFocus()
                                            }
                                        } else {
                                            editGarageCloseTime.error = "Invalid Garage Close Time"
                                            editGarageCloseTime.requestFocus()
                                        }
                                    } else {
                                        editGarageCloseTime.error = "Invalid Garage Close Time"
                                        editGarageCloseTime.requestFocus()
                                    }
                                } else {
                                    editGarageOpenTime.error = "Invalid Garage Open Time"
                                    editGarageOpenTime.requestFocus()
                                }
                            } else {
                                editGarageContact2.error = "Invalid Garage Contact"
                                editGarageContact2.requestFocus()
                            }
                        } else {
                            editGarageContact1.error = "Invalid Garage Contact"
                            editGarageContact1.requestFocus()
                        }
                    } else {
                        editGarageName.error = "Invalid Garage Name"
                        editGarageName.requestFocus()
                    }
                } else {
                    Toast.makeText(context, "Please fill all the details", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        updateGarageLocation = view.findViewById(R.id.updateGarageLocation)

        handler.postDelayed({
            balloon.showAlignTop(updateGarageLocation)
        }, 1500)

        updateGarageLocation.setOnClickListener {
            MaterialAlertDialogBuilder(view.context)
                .setTitle("Update Garage Location")
                .setMessage("Are you sure you want to update your garage location?")
                .setPositiveButton("Update") { _, _ ->
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val locationManager =
                            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            val locationRequest = LocationRequest.create()
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            locationRequest.interval = 30 * 1000
                            locationRequest.fastestInterval = 5 * 1000

                            val builder =
                                LocationSettingsRequest.Builder()
                                    .addLocationRequest(locationRequest)

                            builder.setAlwaysShow(true)

                            val result = LocationServices.getSettingsClient(requireContext())
                                .checkLocationSettings(builder.build())

                            result.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                        if (location != null) {
                                            val geocoder = Geocoder(requireContext())
                                            val addresses = geocoder.getFromLocation(
                                                location.latitude,
                                                location.longitude,
                                                1
                                            )
                                            val garageAddress = addresses!![0].getAddressLine(0)
                                            val garageAddressArray = garageAddress.split(",")
                                            val garageAddressText =
                                                garageAddressArray[0] + ", " + garageAddressArray[1] + ", " + garageAddressArray[2]

                                            if (uid != null) {
                                                val userRef = db.collection("users").document(uid)
                                                userRef.update("garageAddress", garageAddressText)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Garage Location Updated",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        db.collection("users").document(uid)
                                                            .addSnapshotListener { value, error ->
                                                                if (error != null) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Error while fetching data!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                } else {
                                                                    val garageAddress =
                                                                        value?.getString("garageAddress")
                                                                    garageAddressTxt.text =
                                                                        garageAddress
                                                                }
                                                            }
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Garage Location Update Failed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "User not authenticated",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Location not found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
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
                                                requireActivity(),
                                                REQUEST_ENABLE_LOCATION
                                            )
                                        } catch (sendEx: IntentSender.SendIntentException) {
                                            Log.d(
                                                "TAG",
                                                "Error starting resolution for location settings"
                                            )
                                        }
                                    } else {
                                        Log.d("TAG", "getLocation: " + task.exception)
                                    }
                                }
                            }
                        } else {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    val geocoder = Geocoder(requireContext())
                                    val addresses =
                                        geocoder.getFromLocation(
                                            location.latitude,
                                            location.longitude,
                                            1
                                        )
                                    val garageAddress = addresses!![0]?.getAddressLine(0)
                                    val garageAddressArray = garageAddress!!.split(",")
                                    val garageAddressText =
                                        garageAddressArray[0] + ", " + garageAddressArray[1] + ", " + garageAddressArray[2]

                                    if (uid != null) {
                                        val userRef = db.collection("users").document(uid)
                                        userRef.update("garageAddress", garageAddressText)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Garage Location Updated",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                db.collection("users").document(uid)
                                                    .addSnapshotListener { value, error ->
                                                        if (error != null) {
                                                            Toast.makeText(
                                                                context,
                                                                "Error while fetching data!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            val garageAddress =
                                                                value?.getString("garageAddress")
                                                            garageAddressTxt.text = garageAddress
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Garage Location Update Failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "User not authenticated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Location not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Location not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            1
                        )
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        editShowcaseImages = view.findViewById(R.id.editShowcaseImages)

        editShowcaseImages.setOnClickListener {
            multiImagePicker()
        }

        editGarageSpecialization = view.findViewById(R.id.editGarageSpecialization)

        bottomSheetDialog2 = BottomSheetDialog(requireContext())
        val bottomSheetView2 = layoutInflater.inflate(R.layout.editspecialization_bottomsheet, null)
        bottomSheetDialog2.setContentView(bottomSheetView2)

        editGarageSpecialization.setOnClickListener {
            bottomSheetDialog2.show()

            garageSpecialization1 = bottomSheetView2.findViewById(R.id.garageSpecialization1)
            garageSpecialization2 = bottomSheetView2.findViewById(R.id.garageSpecialization2)
            garageSpecialization3 = bottomSheetView2.findViewById(R.id.garageSpecialization3)
            garageSpecialization4 = bottomSheetView2.findViewById(R.id.garageSpecialization4)
            garageSpecialization5 = bottomSheetView2.findViewById(R.id.garageSpecialization5)

            val garageSpecializationAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.specializations)
            )

            garageSpecialization1.setAdapter(garageSpecializationAdapter)
            garageSpecialization2.setAdapter(garageSpecializationAdapter)
            garageSpecialization3.setAdapter(garageSpecializationAdapter)
            garageSpecialization4.setAdapter(garageSpecializationAdapter)
            garageSpecialization5.setAdapter(garageSpecializationAdapter)

            if (uid != null) {
                db.collection("users").document(uid)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            Toast.makeText(
                                context,
                                "Error while fetching data!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val garageSpecialization = value?.getString("garageSpecialization")
                            if (garageSpecialization.isNullOrEmpty()) {
                                garageSpecialization1.hint = "Specialization 1"
                                garageSpecialization2.hint = "Specialization 2"
                                garageSpecialization3.hint = "Specialization 3"
                                garageSpecialization4.hint = "Specialization 4"
                                garageSpecialization5.hint = "Specialization 5"
                            }
                        }
                    }
            } else {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            }

            updateGarageSpecialization =
                bottomSheetView2.findViewById(R.id.updateGarageSpecialization)

            updateGarageSpecialization.setOnClickListener {
                val garageSpecialization1Text = garageSpecialization1.text.toString()
                val garageSpecialization2Text = garageSpecialization2.text.toString()
                val garageSpecialization3Text = garageSpecialization3.text.toString()
                val garageSpecialization4Text = garageSpecialization4.text.toString()
                val garageSpecialization5Text = garageSpecialization5.text.toString()

                if (garageSpecialization1Text.isNotEmpty() && garageSpecialization2Text.isNotEmpty() && garageSpecialization3Text.isNotEmpty() && garageSpecialization4Text.isNotEmpty() && garageSpecialization5Text.isNotEmpty()){
                    if (garageSpecialization1Text!=garageSpecialization2Text && garageSpecialization1Text!=garageSpecialization3Text && garageSpecialization1Text!=garageSpecialization4Text
                        && garageSpecialization1Text!=garageSpecialization5Text && garageSpecialization2Text!=garageSpecialization3Text && garageSpecialization2Text!=garageSpecialization4Text
                        && garageSpecialization2Text!=garageSpecialization5Text && garageSpecialization3Text!=garageSpecialization4Text && garageSpecialization3Text!=garageSpecialization5Text
                        && garageSpecialization4Text!=garageSpecialization5Text){
                        if (garageSpecialization1Text!="Specialization 1" && garageSpecialization2Text!="Specialization 2" && garageSpecialization3Text!="Specialization 3" && garageSpecialization4Text!="Specialization 4" && garageSpecialization5Text!="Specialization 5") {
                            if (uid != null) {
                                db.collection("users").document(uid)
                                    .update(
                                        "garageSpecialization",
                                        garageSpecialization1Text + ", " + garageSpecialization2Text + ", " + garageSpecialization3Text + ", " + garageSpecialization4Text + ", " + garageSpecialization5Text
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Garage Specialization Updated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        db.collection("users").document(uid)
                                            .addSnapshotListener { value, error ->
                                                if (error != null) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error while fetching data!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    val garageSpecialization =
                                                        value?.getString("garageSpecialization")
                                                    if (garageSpecialization.isNullOrEmpty()) {
                                                        specialization1.text = "◆  Specialization 1"
                                                        specialization2.text = "◆  Specialization 2"
                                                        specialization3.text = "◆  Specialization 3"
                                                        specialization4.text = "◆  Specialization 4"
                                                        specialization5.text = "◆  Specialization 5"
                                                    } else {
                                                        val garageSpecializationArray =
                                                            garageSpecialization?.split(", ")
                                                        specialization1.text =
                                                            "◆  " + garageSpecializationArray?.get(0)
                                                        specialization2.text =
                                                            "◆  " + garageSpecializationArray?.get(1)
                                                        specialization3.text =
                                                            "◆  " + garageSpecializationArray?.get(2)
                                                        specialization4.text =
                                                            "◆  " + garageSpecializationArray?.get(3)
                                                        specialization5.text =
                                                            "◆  " + garageSpecializationArray?.get(4)
                                                    }
                                                }
                                            }
                                        bottomSheetDialog2.dismiss()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Garage Specialization Update Failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "User not authenticated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        else{
                            Toast.makeText(context, "Please fill all the details", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(context, "Specializations cannot be same", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(context, "Please fill all the details", Toast.LENGTH_SHORT).show()
                }
            }

        }

        deleteShowcaseImages = view.findViewById(R.id.deleteShowcaseImages)

        deleteShowcaseImages.setOnClickListener {
            MaterialAlertDialogBuilder(view.context)
                .setTitle("Delete Showcase Images")
                .setMessage("Are you sure you want to delete your showcase images?")
                .setPositiveButton("Delete") { _, _ ->
                    if (uid != null) {

                        db.collection("users").document(uid)
                            .update("showcaseImages", FieldValue.delete())
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Showcase Images Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()

                                bannerscroll1.setImageResource(R.drawable.blank)
                                bannerscroll2.setImageResource(R.drawable.blank)
                                bannerscroll3.setImageResource(R.drawable.blank)
                                bannerscroll4.setImageResource(R.drawable.blank)
                                bannerscroll5.setImageResource(R.drawable.blank)
                                bannerscroll6.setImageResource(R.drawable.blank)
                                bannerscroll7.setImageResource(R.drawable.blank)
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Showcase Images Delete Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "User not authenticated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                }
                .show()
        }


        return view
    }

    private fun imagePicker(){
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .maxResultSize(1080, 1080)
            .start()
    }
    
    private fun multiImagePicker(){
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choose Pictures")
                , REQUESTS_CODE
            )
        }
        else { // For latest versions API LEVEL 19+
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUESTS_CODE);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ImagePicker.REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        selectedImageUri = data?.data

                        if (whichImage == "owner") {
                            uploadImageToFirebaseStorage("photo", ownerImage)
                        } else if (whichImage == "cover") {
                            uploadImageToFirebaseStorage("cover", coverImage)
                        }
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            REQUESTS_CODE -> {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val maxImageLimit = 7

                if (resultCode == Activity.RESULT_OK) {
                    db.collection("users").document(uid!!)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val showcaseImages = documentSnapshot.get("showcaseImages") as List<String>?
                            val initialImagesCount = showcaseImages?.size ?: 0

                            if (initialImagesCount >= maxImageLimit) {
                                Toast.makeText(context, "Max limit of images is 7", Toast.LENGTH_SHORT).show()
                            } else {
                                val count = data?.clipData?.itemCount ?: 0
                                for (i in 0 until count) {
                                    val imageUri = data?.clipData?.getItemAt(i)?.uri
                                    if (imageUri != null) {
                                        val filename = UUID.randomUUID().toString()
                                        val ref = storageReference.child("images/$filename")

                                        ref.putFile(imageUri)
                                            .addOnSuccessListener { taskSnapshot ->
                                                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                                    Log.d("Image", "File Location: $uri")
                                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                                    if (uid != null) {
                                                        db.collection("users").document(uid)
                                                            .update(
                                                                "showcaseImages",
                                                                FieldValue.arrayUnion(uri.toString())
                                                            )
                                                            .addOnSuccessListener {
                                                                Log.d("Image", "File Location: $it")

                                                                val imageViews = listOf(bannerscroll1, bannerscroll2, bannerscroll3, bannerscroll4, bannerscroll5, bannerscroll6, bannerscroll7)

                                                                for (i in 0 until imageViews.size) {
                                                                    if (i < showcaseImages?.size ?: 0) {
                                                                        Picasso.get()
                                                                            .load(showcaseImages?.get(i)?.toUri())
                                                                            .placeholder(R.drawable.blank)
                                                                            .into(imageViews[i])
                                                                    } else {
                                                                        imageViews[i].setImageResource(R.drawable.blank)
                                                                    }
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                Log.d("Image", "File Location: $it")
                                                            }
                                                    } else {
                                                        Log.d("Image", "File Location: ")
                                                    }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Log.d("Image", "File Location: $it")
                                            }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error while fetching data!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
                else -> {
                Toast.makeText(context, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageToFirebaseStorage(imageType: String, imageView: ImageView) {
        val selectedImageUri = selectedImageUri
        if (selectedImageUri != null) {
            val filename = UUID.randomUUID().toString()
            val ref = storageReference.child("images/$filename")
            ref.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Image", "File Location: $uri")
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            db.collection("users").document(uid)
                                .update(imageType, uri.toString())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Image Uploaded",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.blank)
                                        .into(imageView)

                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Image Upload Failed",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                        } else {
                            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        var showcaseImages: List<String>?

        if (uid != null) {
            db.collection("users").document(uid)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(
                            context,
                            "Error while fetching data!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showcaseImages = value?.get("showcaseImages") as List<String>?

                        val imageViews = listOf(bannerscroll1, bannerscroll2, bannerscroll3, bannerscroll4, bannerscroll5, bannerscroll6, bannerscroll7)

                        for (i in 0 until imageViews.size) {
                            if (i < showcaseImages?.size ?: 0) {
                                Picasso.get()
                                    .load(showcaseImages?.get(i)?.toUri())
                                    .placeholder(R.drawable.blank)
                                    .into(imageViews[i])
                            } else {
                                imageViews[i].setImageResource(R.drawable.blank)
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        super.onResume()
    }
}