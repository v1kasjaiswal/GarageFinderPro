package dev.falcon.garagefinderpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class UserRequestsActivity : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<UserRequestsRecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerview: RecyclerView

    lateinit var requestedStatus : AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.userrequests_activity, container, false)

        recyclerview = view.findViewById(R.id.userRequestsRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = UserRequestsRecyclerAdapter()
        recyclerview.adapter = adapter

        requestedStatus = view.findViewById(R.id.requestedStatus)

        var statuses = arrayOf<String>("New Requests", "Pending", "In Progress", "Declined", "Completed", "Cancelled")
        val statusTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            statuses
        )
        requestedStatus.setAdapter(statusTypeAdapter)

        requestedStatus.setOnItemClickListener { _, _, i, _ ->
            var status = statuses[i]

            if (recyclerview.adapter is UserRequestsRecyclerAdapter) {

                val adapter = recyclerview.adapter as UserRequestsRecyclerAdapter
                adapter.filterRequestedData(status)
            }
        }

        requestedStatus.setText("New Requests", false)

        return view
    }


}