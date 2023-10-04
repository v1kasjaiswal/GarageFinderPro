package dev.falcon.garagefinderpro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.squareup.picasso.Picasso
import io.karn.notify.Notify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

val REQUEST_ENABLE_LOCATION = 141

class UserHomeActivity : Fragment() {

    lateinit var horizontalScrollView: HorizontalScrollView
    lateinit var linearLayout: LinearLayout
    lateinit var quoteview : TextView
    lateinit var refreshLocation : ImageView

    val storage = FirebaseStorage.getInstance()

    lateinit var petrolPriceText : TextView
    lateinit var dieselPriceText : TextView
    lateinit var cngPriceText : TextView
    lateinit var cityName : TextView

    lateinit var banner1 : ImageView
    lateinit var banner2 : ImageView
    lateinit var banner3 : ImageView
    lateinit var banner4 : ImageView
    lateinit var banner5 : ImageView
    lateinit var banner6 : ImageView
    lateinit var banner7 : ImageView

    lateinit var fluidlevels : ImageView
    lateinit var batterycare : ImageView
    lateinit var tyrecare : ImageView
    lateinit var wheelalign :  ImageView
    lateinit var radiatorcare : ImageView
    lateinit var brakecare : ImageView

    lateinit var settings : ImageView

    var currentIndex = 0
    var childCount = 0

    val handler = Looper.getMainLooper().let { Handler(it) }

    private lateinit var userLocation : TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var articleImage : ImageView
    lateinit var articleTitle : TextView
    lateinit var articleDescription : TextView

    var db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.userhome_activity, container, false)

        var bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.caretips_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        horizontalScrollView = view.findViewById(R.id.horizontalScrollView)
        linearLayout = view.findViewById(R.id.linearLayout)

        quoteview  = view.findViewById(R.id.quoteview)

        childCount = linearLayout.childCount

        handler.postDelayed(runnable, 3000)

        userLocation = view.findViewById(R.id.userLocation)
        userLocation.isSelected = true

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        refreshLocation = view.findViewById(R.id.imageView5)

        val balloon: Balloon = createBalloon(requireContext()) {
            setArrowSize(10)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setCornerRadius(4f)
            setPadding(10)
            setAutoDismissDuration(1500L)
            setMargin(10)
            setCornerRadius(6f)
            setText("Click Here to Refresh Location!")
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.black)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }

        handler.postDelayed({
            balloon.showAlignBottom(refreshLocation)
        }, 1500)

        getLocation()

        refreshLocation.setOnClickListener {
            getLocation()
            balloon.showAlignBottom(refreshLocation)
        }

        petrolPriceText = view.findViewById(R.id.petrolPriceText)
        dieselPriceText = view.findViewById(R.id.dieselPriceText)
        cngPriceText = view.findViewById(R.id.cngPriceText)

        banner1 = view.findViewById(R.id.homescrollimg1)
        banner2 = view.findViewById(R.id.homescrollimg2)
        banner3 = view.findViewById(R.id.homescrollimg3)
        banner4 = view.findViewById(R.id.homescrollimg4)
        banner5 = view.findViewById(R.id.homescrollimg5)
        banner6 = view.findViewById(R.id.homescrollimg6)
        banner7 = view.findViewById(R.id.homescrollimg7)

        fluidlevels = view.findViewById(R.id.fluidlevels)
        batterycare = view.findViewById(R.id.batterycare)
        tyrecare = view.findViewById(R.id.tyrecare)
        wheelalign = view.findViewById(R.id.wheelalign)
        radiatorcare = view.findViewById(R.id.radiatorcare)
        brakecare = view.findViewById(R.id.brakecare)

        loadImages("homescrollimg1.jpg", banner1)
        loadImages("homescrollimg2.jpg", banner2)
        loadImages("homescrollimg3.jpg", banner3)
        loadImages("homescrollimg4.jpg", banner4)
        loadImages("homescrollimg5.jpg", banner5)
        loadImages("homescrollimg6.jpg", banner6)
        loadImages("homescrollimg7.jpg", banner7)

        loadImages("fluidlevels.jpg", fluidlevels)
        loadImages("batterycare.jpg", batterycare)
        loadImages("tyrecare.jpg", tyrecare)
        loadImages("wheelalign.jpeg", wheelalign)
        loadImages("radiatorcare.jpg", radiatorcare)
        loadImages("brakecare.jpg", brakecare)


        fluidlevels.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("fluidlevels.jpg", articleImage)

                    articleTitle.text = "Fluid Levels"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("fluidlevels").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }

        batterycare.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("batterycare.jpg", articleImage)

                    articleTitle.text = "Battery Care"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("batterycare").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }

        tyrecare.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("tyrecare.jpg", articleImage)

                    articleTitle.text = "Tyre Care"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("tyrecare").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }

        wheelalign.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("wheelalign.jpeg", articleImage)

                    articleTitle.text = "Wheel Alignment"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("wheelalign").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }

        radiatorcare.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("radiatorcare.jpg", articleImage)

                    articleTitle.text = "Radiator Care"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("radiatorcare").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }

        brakecare.setOnClickListener {
            bottomSheetDialog.show()

            db.collection("caretips")
                .get()
                .addOnSuccessListener { document ->
                    articleImage = bottomSheetView.findViewById(R.id.articleImage)
                    articleTitle = bottomSheetView.findViewById(R.id.articleTopic)
                    articleDescription = bottomSheetView.findViewById(R.id.articleDescription)

                    loadImages("brakecare.jpg", articleImage)

                    articleTitle.text = "Brake Care"

                    if (document != null) {
                        articleDescription.text = document.documents[0].data?.get("brakecare").toString()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }


        settings = view.findViewById(R.id.settings)

        settings.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), settings)
            popupMenu.inflate(R.menu.popup_menu)

            // Set a click listener for the popup menu items
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.notifications -> {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        startActivity(intent)
                        true
                    }
                    R.id.privacy -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Privacy Policy")
                            .setMessage("At Garage Finder Pro!, we prioritize your privacy. When using our Android app, Garage Finder Pro!, we may collect and utilize your precise location data solely for providing towing services and sharing it with garage owners as required. This information assists us in offering efficient and timely services. We are committed to protecting your data; however, please note that absolute security cannot be guaranteed online. You have the option to opt-out of location sharing, although it may affect the app's functionality. By using our app, you agree to these terms. If you have any questions or concerns, please don't hesitate to contact us.")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()

                        true
                    }
                    R.id.about ->{
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("About Us")
                            .setMessage("Hi, I am Vikas Jaiswal, the developer of this app. I am a Computer Science Student. I have developed this app to help people find the best garages for their automobile's need. This app also helps users to get the fuel prices in their city. This app also provides some tips to take care of your vehicle. I hope you like this app. ")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()

        }

        return view
    }

    private val runnable = Runnable { scrollToNextImage() }

    val quotes = arrayOf(
        "FIXING FAST, ROLLING SMOOTH",
        "REVIVE YOUR RIDE",
        "FIX, DRIVE, THRIVE",
        "TURNING WRENCHES, RESTORING MOTION",
        "BOLTS TIGHTENED, SAFETY ENLIGHTENED",
        "FIXING CARS, FIXING LIVES",
        "RESCUING YOUR ADVENTURES",
    )



    private fun scrollToNextImage() {
        val nextIndex = (currentIndex + 1) % childCount
        val nextChild = linearLayout.getChildAt(nextIndex) as CardView

        horizontalScrollView.smoothScrollTo(nextChild.left, 0)

        quoteview.text = quotes[nextIndex]

        currentIndex = nextIndex

        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 30 * 1000
                locationRequest.fastestInterval = 5 * 1000

                val builder =  LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                builder.setAlwaysShow(true)

                val result = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(builder.build())

                result.addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                val geocoder = android.location.Geocoder(requireContext())
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val address = addresses?.get(0)?.getAddressLine(0)
                                val city = addresses?.get(0)?.locality
                                val state = addresses?.get(0)?.adminArea
                                val country = addresses?.get(0)?.countryName
                                userLocation.text = "$city, $state, $country"

                                val cityName = view?.findViewById(R.id.cityName) as TextView
                                cityName.text = city.toString()

                                val fuelPriceAPI = FuelPriceAPI()
                                fuelPriceAPI.getFuelPrices(state.toString().lowercase(), city.toString().lowercase()) { petrolPrice, dieselPrice, cngPrice ->
                                    petrolPriceText.text = "$petrolPrice Rs."
                                    dieselPriceText.text = "$dieselPrice Rs."
                                    cngPriceText.text = "$cngPrice Rs."
                                }

                            }
                            else {
                                userLocation.text = "Location Not Found"
                            }
                        }
                            .addOnFailureListener {
                                userLocation.text = "Location Permission Denied"
                            }
                    }
                    else {
                        if (task.exception is ResolvableApiException) {
                            try {
                                val resolvable = task.exception as ResolvableApiException
                                resolvable.startResolutionForResult(requireActivity(), REQUEST_ENABLE_LOCATION)
                            } catch (sendEx: IntentSender.SendIntentException) {
                                Log.d("TAG", "Error starting resolution for location settings")
                            }
                        } else {
                            Log.d("TAG", "getLocation: " + task.exception)
                        }
                    }
                }
            }
            else {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val geocoder = android.location.Geocoder(requireContext())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.get(0)?.getAddressLine(0)
                        val city = addresses?.get(0)?.locality
                        val state = addresses?.get(0)?.adminArea
                        val country = addresses?.get(0)?.countryName
                        userLocation.text = "$city, $state, $country"

                        val cityName = view?.findViewById(R.id.cityName) as TextView
                        cityName.text = city.toString()

                        val fuelPriceAPI = FuelPriceAPI()
                        fuelPriceAPI.getFuelPrices(state.toString().lowercase(), city.toString().lowercase()) { petrolPrice, dieselPrice, cngPrice ->
                            petrolPriceText.text = "$petrolPrice Rs."
                            dieselPriceText.text = "$dieselPrice Rs."
                            cngPriceText.text = "$cngPrice Rs."
                        }
                    }
                    else {
                        userLocation.text = "Location Not Found"
                    }
                }
                    .addOnFailureListener {
                        userLocation.text = "Location Permission Denied"
                    }

            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location not enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadImages(imagename : String, banner : ImageView){
        try {
            val storageReference = storage.reference
            val imageRef = storageReference.child("Banners/$imagename")

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.blank)
                    .into(banner)

            }.addOnFailureListener {
                Log.d("TAG", "loadImages: " + it.message)
            }
        }
        catch (e : Exception){
            Log.d("TAG", "loadImages: " + e.message)
        }
    }
}