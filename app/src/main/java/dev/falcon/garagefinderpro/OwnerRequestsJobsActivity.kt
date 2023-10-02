package dev.falcon.garagefinderpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class OwnerRequestsJobsActivity : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<OwnerRequestsJobsRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    lateinit var statusType : AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.ownerrequestsjobs_activity, container, false)

        recyclerview = view.findViewById(R.id.ownerRequestsJobsRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = OwnerRequestsJobsRecyclerAdapter()
        recyclerview.adapter = adapter

        statusType = view.findViewById(R.id.statusType)

        var statuses = arrayOf<String>("New Requests","Pending", "In Progress", "Declined", "Completed")
        val statusTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            statuses
        )
        statusType.setAdapter(statusTypeAdapter)

        statusType.setOnItemClickListener { _, _, i, _ ->
            var status = statuses[i]

            if (recyclerview.adapter is OwnerRequestsJobsRecyclerAdapter) {

                val adapter = recyclerview.adapter as OwnerRequestsJobsRecyclerAdapter
                adapter.filterData(status)
            }
        }

        statusType.setText("New Requests", false)


        return view
    }

}