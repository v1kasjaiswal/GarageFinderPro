package dev.falcon.garagefinderpro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

private val REQUEST_ENABLE_LOCATION = 141

class UserHomeActivity : Fragment() {

    lateinit var horizontalScrollView: HorizontalScrollView
    lateinit var linearLayout: LinearLayout
    lateinit var quoteview : TextView
    lateinit var refreshLocation : ImageView

    lateinit var petrolPriceText : TextView
    lateinit var dieselPriceText : TextView
    lateinit var cngPriceText : TextView
    lateinit var cityName : TextView

    var currentIndex = 0
    var childCount = 0

    val handler = Looper.getMainLooper().let { Handler(it) }

    private lateinit var userLocation : TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.userhome_activity, container, false)

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
            setAutoDismissDuration(5000L)
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

}