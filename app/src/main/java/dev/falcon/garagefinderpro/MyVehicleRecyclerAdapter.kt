package dev.falcon.garagefinderpro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyVehicleRecyclerAdapter : RecyclerView.Adapter<MyVehicleRecyclerAdapter.ViewHolder>()
{
    var vnames = listOf<String>()
    var vnumbers = listOf<String>()
    var vtypes = listOf<String>()
    var vmodels = listOf<String>()
    var vfueltype = listOf<String>()

    var db = Firebase.firestore

    var auth = FirebaseAuth.getInstance()

    init {
        db.collection("users").document(auth.currentUser!!.uid).collection("vehicles")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    vnames = vnames + document.data["vehicleName"].toString()
                    vnumbers = vnumbers + document.data["vehicleNumber"].toString()
                    vtypes = vtypes + document.data["vehicleType"].toString()
                    vmodels = vmodels + document.data["vehicleModel"].toString()
                    vfueltype = vfueltype + document.data["fuelType"].toString()
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("TAG", "onCreateView: ${it.message}")
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVehicleRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.myvehicles_resource, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return vnames.size
    }

    override fun onBindViewHolder(holder: MyVehicleRecyclerAdapter.ViewHolder, position: Int) {
        holder.vname.text = vnames[position]
        holder.vnumber.text = vnumbers[position]
        holder.vtype.text = vtypes[position]
        holder.vmodel.text = vmodels[position]
        holder.vfueltype.text = vfueltype[position]

        holder.removeVehicle.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this vehicle?")
                .setPositiveButton("Delete") { _, _ ->
            db.collection("users").document(auth.currentUser!!.uid).collection("vehicles").document(vnumbers[position].toString())
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Vehicle Removed", Toast.LENGTH_SHORT).show()
                    vnames = vnames - vnames[position]
                    vnumbers = vnumbers - vnumbers[position]
                    vtypes = vtypes - vtypes[position]
                    vmodels = vmodels - vmodels[position]
                    vfueltype = vfueltype - vfueltype[position]
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "Error removing document", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(holder.itemView.context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()
        }

    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var vname: TextView
        lateinit var vnumber: TextView
        lateinit var vtype: TextView
        lateinit var vfueltype: TextView
        lateinit var vmodel: TextView
        lateinit var removeVehicle: ImageView

        init {
            vname = itemView.findViewById(R.id.vname)
            vnumber = itemView.findViewById(R.id.vnumber)
            vtype = itemView.findViewById(R.id.vtype)
            vfueltype = itemView.findViewById(R.id.vfueltype)
            vmodel = itemView.findViewById(R.id.vmodel)
            removeVehicle = itemView.findViewById(R.id.removeVehicle)
        }
    }


}