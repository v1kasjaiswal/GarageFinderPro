package dev.falcon.garagefinderpro

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.ownerprofile_activity, container, false)

        val auth = FirebaseAuth.getInstance()

        ownerName = view.findViewById(R.id.ownerName)
        ownerImage = view.findViewById(R.id.ownerImage)
        changeOwnerImage = view.findViewById(R.id.changeOwnerImage)
        coverImage = view.findViewById(R.id.coverImage)
        changeCoverImage = view.findViewById(R.id.changeCoverImage)

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
                } else {
                    Toast.makeText(context, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("OwnerProfile", "Error getting documents: $exception")
            }

        changeOwnerImage.setOnClickListener {
            imagePicker()
        }

        changeCoverImage.setOnClickListener {
            imagePicker()
        }

        return view
    }

    private fun imagePicker(){
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ImagePicker.REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        selectedImageUri = data?.data
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

    private fun uploadImageToFirebaseStorage(loadImageHere : ImageView) {
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
                                    Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT)
                                        .show()
                                    Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.blank)
                                        .into(loadImageHere)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Image Upload Failed", Toast.LENGTH_SHORT)
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
