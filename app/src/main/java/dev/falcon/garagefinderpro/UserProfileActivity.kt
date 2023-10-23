package dev.falcon.garagefinderpro

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID
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
    lateinit var vehicleModel: AutoCompleteTextView
    lateinit var fuelType: AutoCompleteTextView

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MyVehicleRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    lateinit var updateUserImage : ImageView

    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    lateinit var addContact : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.userprofile_activity, container, false)

        userName = view.findViewById(R.id.userName)
        userName.isSelected = true

        userImage = view.findViewById(R.id.userImage)

        var auth = FirebaseAuth.getInstance()

        storageReference = FirebaseStorage.getInstance().reference

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
            vehicleModel = bottomSheetView.findViewById(R.id.vehicleModel)
            fuelType = bottomSheetView.findViewById(R.id.fuelType)

            val vehicleTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.vehicleType)
            )
            vehicleType.setAdapter(vehicleTypeAdapter)

            vehicleType.setOnItemClickListener { _, _, i, _ ->
                val type = vehicleTypeAdapter.getItem(i).toString()

                if (type.equals("Car (4-Wheeler)")) {

                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.CarModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
                else if (type.equals("Bike (2-Wheeler)")){
                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.BikeModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
                else if (type.equals("Scooter (2-Wheeler)")){
                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.ScooterModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
                else if (type.equals("Rickshaw (3-Wheeler)")){
                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.RickshawModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
                else if (type.equals("Truck")){
                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.TruckModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
                else if (type.equals("Others")){
                    val vehicleModelAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        resources.getStringArray(R.array.OtherModels)
                    )
                    vehicleModel.setAdapter(vehicleModelAdapter)
                }
            }

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
                var vehicleModelTxt = vehicleModel.text.toString()
                var fuelTypeTxt = fuelType.text.toString()

                if (vehicleNameTxt.isNotEmpty() && vehicleNumberTxt.isNotEmpty() && vehicleTypeTxt.isNotEmpty() && vehicleModelTxt.isNotEmpty() && fuelTypeTxt.isNotEmpty())
                {
                    if (vehicleNameTxt.matches("^[a-zA-Z]+[a-zA-Z0-9\\s]*[a-zA-Z0-9]\$".toRegex())){
                        if (vehicleNumberTxt.matches("^[A-Za-z]{2}[0-9]{2}[A-Za-z]{2}[0-9]{4}\$".toRegex()) || vehicleNumberTxt.matches("^\\d{2}BH\\d{4}[A-Za-z]{2}\$".toRegex())){
                            if (vehicleTypeTxt in resources.getStringArray(R.array.vehicleType)){
                                if (vehicleModelTxt.isNotEmpty() && vehicleModelTxt!="Vehicle Model") {
                                    if (fuelTypeTxt in resources.getStringArray(R.array.fuelType)) {
                                        val vehicle = hashMapOf(
                                            "vehicleName" to vehicleName.text.toString()
                                                .capitalize(),
                                            "vehicleNumber" to vehicleNumber.text.toString()
                                                .uppercase(),
                                            "vehicleType" to vehicleType.text.toString(),
                                            "vehicleModel" to vehicleModel.text.toString(),
                                            "fuelType" to fuelType.text.toString()
                                        )

                                        db.collection("users").document(auth.currentUser!!.uid)
                                            .collection("vehicles")
                                            .document(vehicleNumber.text.toString().uppercase())
                                            .set(vehicle)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Vehicle Added",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                bottomSheetDialog.dismiss()

                                                db.collection("users")
                                                    .document(auth.currentUser!!.uid)
                                                    .collection("vehicles")
                                                    .addSnapshotListener { _, error ->
                                                        if (error != null) {
                                                            Toast.makeText(
                                                                context,
                                                                "Error while fetching data!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            adapter = MyVehicleRecyclerAdapter()
                                                            recyclerview.adapter = adapter
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    context,
                                                    "Error while adding Vehicle!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        fuelType.error = "Please enter a valid fuel type!"
                                        fuelType.requestFocus()
                                    }
                                }
                                else{
                                    vehicleModel.error = "Please enter a valid vehicle model!"
                                    vehicleModel.requestFocus()
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

        updateUserImage = view.findViewById(R.id.updateUserImage)

        updateUserImage.setOnClickListener {
            imagePicker()
        }

        addContact = view.findViewById(R.id.addContact)

        addContact.setOnClickListener {
            // Create a custom view for the dialog
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.addcontact_dialog, null)
            val contactNumberEditText = dialogView.findViewById<EditText>(R.id.contactNumber)

//            get the contact from database and if it is not null then set the texxt of contactNumberEditText  to contact
            db.collection("users").document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        var contact = document.data?.get("contact").toString()
                        if (contact.isNotEmpty() && contact.matches("^[6-9]\\d{9}\$".toRegex())){
                            contactNumberEditText.setText(contact)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }


            val alertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Contact Number")
                .setView(dialogView)
                .setPositiveButton("Save", null) // Set positive button with null click listener
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val contactNumber = contactNumberEditText.text.toString()

                if (contactNumber.isNotEmpty() && contactNumber.matches("^[6-9]\\d{9}\$".toRegex())) {
                    val userDocRef = db.collection("users").document(auth.currentUser!!.uid)

                    userDocRef.update("contact", contactNumber)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Contact Added",
                                Toast.LENGTH_SHORT
                            ).show()
                            alertDialog.dismiss() // Dismiss the dialog after successful addition
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error while adding Contact!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    contactNumberEditText.error = "Please enter a valid contact number!"
                    contactNumberEditText.requestFocus()
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ImagePicker.REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        selectedImageUri = data?.data

                        uploadImageToFirebaseStorage()
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            else -> {
                Toast.makeText(context, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageToFirebaseStorage() {
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
                                .update("photo", uri.toString())
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
                                        .into(userImage)

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

}
