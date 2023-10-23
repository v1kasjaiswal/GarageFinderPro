package dev.falcon.garagefinderpro

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserSearchActivity : Fragment() {

    lateinit var searchView : SearchView

    lateinit var filterGarage : ImageView

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    lateinit var filterTowing : AutoCompleteTextView
    lateinit var filterMinCost : AutoCompleteTextView

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.usersearch_activity, container, false)

        recyclerview = view.findViewById(R.id.searchRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = SearchRecyclerAdapter()
        recyclerview.adapter = adapter

        searchView = view.findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val queryString = query ?: ""
                adapter?.let {
                    it as SearchRecyclerAdapter
                    it.searchFilter(queryString)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Ensure you have access to the 'newText' parameter
                val queryString = newText ?: ""
                adapter?.let {
                    it as SearchRecyclerAdapter
                    it.searchFilter(queryString)
                }
                return false
            }
        })

        filterGarage = view.findViewById(R.id.filterGarage)



        filterGarage.setOnClickListener {
            val dialogView =  inflater.inflate(R.layout.filter_dialog, null)

            filterTowing = dialogView.findViewById(R.id.filterTowing)
            filterMinCost = dialogView.findViewById(R.id.filterMinCost)

            val filterTowingAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.garagetows)
            )
            filterTowing.setAdapter(filterTowingAdapter)

            val filterMinCostArray : ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.garagecharges)
            )
            filterMinCost.setAdapter(filterMinCostArray)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter")
                .setMessage("Filter the garage list")
                .setView(dialogView)
                .setPositiveButton("Filter") { _, _ ->
                    val filterTowingText = filterTowing.text.toString()
                    val filterMinCostText = filterMinCost.text.toString()

                    if (filterTowingText in resources.getStringArray(R.array.garagetows)) {
                        if (filterMinCostText in resources.getStringArray(R.array.garagecharges)) {
                                adapter?.let {
                                    it as SearchRecyclerAdapter
                                    it.filterGarage(
                                        filterTowingText,
                                        filterMinCostText)

                                    Log.d("UserSearchActivity", "Positive Button Clicked")
                                }
                        } else {
                            filterMinCost.error = "Invalid Cost"
                            filterMinCost.requestFocus()
                        }
                    } else {
                        filterTowing.error = "Invalid Towing"
                        filterTowing.requestFocus()
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Log.d("UserSearchActivity", "Negative Button Clicked")
                }

                .show()
        }

        return view
    }
}