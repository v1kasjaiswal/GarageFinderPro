package dev.falcon.garagefinderpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ViewProductActivity : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<ViewProductRecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerview: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.viewproduct_activity, container, false)

        recyclerview = view.findViewById(R.id.productsRecyclerView)

        layoutManager = LinearLayoutManager(context)
        recyclerview.layoutManager = layoutManager

        adapter = ViewProductRecyclerAdapter()
        recyclerview.adapter = adapter





        return view
    }


}