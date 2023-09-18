package dev.falcon.garagefinderpro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlin.math.log

class UserProfileActivity : Fragment() {

    lateinit var userName : TextView
    lateinit var userImage : ImageView
    private var db = Firebase.firestore

    lateinit var addVehicle : ImageView
    lateinit var saveVehicle : Button

    lateinit var bottomSheetDialog: BottomSheetDialog

    lateinit var vehicleName: EditText
    lateinit var vehicleNumber: EditText
    lateinit var vehicleType: AutoCompleteTextView
    lateinit var fuelType: AutoCompleteTextView

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MyVehicleRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.userprofile_activity, container, false)

        userName = view.findViewById(R.id.userName)
        userName.isSelected = true

        userImage = view.findViewById(R.id.userImage)

        var auth = FirebaseAuth.getInstance()

        db.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    var userName = document.data?.get("name").toString()
                    this.userName.text = userName

                    var userImage = document.data?.get("photo").toString()
                    Log.d("userImage", userImage)
                    if (userImage.isEmpty() || userImage == "null"){
                        this.userImage.setImageResource(R.drawable.blank)
                    }
                    else{
                        Picasso.get()
                            .load(userImage.toUri())
                            .placeholder(R.drawable.blank)
                            .into(this.userImage)
                    }
                } else {
                    Toast.makeText(context, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }

        recyclerview = view.findViewById(R.id.myvehiclesrecyclerview)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = MyVehicleRecyclerAdapter()

        recyclerview.adapter = adapter

        addVehicle = view.findViewById(R.id.addVehicle)

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.addvehicle_bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        addVehicle.setOnClickListener {
            bottomSheetDialog.show()

            saveVehicle = bottomSheetView.findViewById(R.id.saveVehicle)

            vehicleName = bottomSheetView.findViewById(R.id.vehicleName)
            vehicleNumber = bottomSheetView.findViewById(R.id.vehicleNumber)
            vehicleType = bottomSheetView.findViewById(R.id.vehicleType)
            fuelType = bottomSheetView.findViewById(R.id.fuelType)

            val vehicelTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.vehicleType)
            )
            vehicleType.setAdapter(vehicelTypeAdapter)

            val fuelTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.fuelType)
            )
            fuelType.setAdapter(fuelTypeAdapter)

            saveVehicle.setOnClickListener {
                var vehicleNameTxt = vehicleName.text.toString()
                var vehicleNumberTxt = vehicleNumber.text.toString()
                var vehicleTypeTxt = vehicleType.text.toString()
                var fuelTypeTxt = fuelType.text.toString()

                if (vehicleNameTxt.isNotEmpty() || vehicleNumberTxt.isNotEmpty() || vehicleTypeTxt.isNotEmpty() || fuelTypeTxt.isNotEmpty())
                {
                    if (vehicleNameTxt.matches("^[a-zA-Z]+[a-zA-Z0-9\\s]*[a-zA-Z0-9]\$".toRegex())){
                        if (vehicleNumberTxt.matches("^[A-Za-z]{2}[0-9]{2}[A-Za-z]{2}[0-9]{4}\$".toRegex()) || vehicleNumberTxt.matches("^\\d{2}BH\\d{4}[A-Za-z]{2}\$".toRegex())){
                            if (vehicleTypeTxt in resources.getStringArray(R.array.vehicleType)){
                                if (fuelTypeTxt.matches("^[A-Za-z][A-Za-z\\\\s]*[A-Za-z]\$".toRegex())){
                                    val vehicle = hashMapOf(
                                        "vehicleName" to vehicleName.text.toString(),
                                        "vehicleNumber" to vehicleNumber.text.toString(),
                                        "vehicleType" to vehicleType.text.toString(),
                                        "fuelType" to fuelType.text.toString()
                                    )

                                    db.collection("users").document(auth.currentUser!!.uid).collection("vehicles").document(vehicleName.text.toString())
                                        .set(vehicle)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Vehicle Added", Toast.LENGTH_SHORT).show()
                                            bottomSheetDialog.dismiss()

                                            db.collection("users").document(auth.currentUser!!.uid).collection("vehicles")
                                                .addSnapshotListener { _, error ->
                                                    if (error != null){
                                                        Toast.makeText(context, "Error while fetching data!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    else{
                                                        adapter = MyVehicleRecyclerAdapter()
                                                        recyclerview.adapter = adapter
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error while adding Vehicle!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                else{
                                    fuelType.error = "Please enter a valid fuel type!"
                                    fuelType.requestFocus()
                                }
                            }
                            else{
                                vehicleType.error = "Please enter a valid vehicle type!"
                                vehicleType.requestFocus()
                            }
                        }
                        else{
                            vehicleNumber.error = "Please enter a valid vehicle number!"
                            vehicleNumber.requestFocus()
                        }
                    }
                    else{
                        vehicleName.error = "Please enter a valid vehicle name!"
                        vehicleName.requestFocus()
                    }
                }
                else{
                    Toast.makeText(context, "Please fill all the fields!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return view
    }

}
