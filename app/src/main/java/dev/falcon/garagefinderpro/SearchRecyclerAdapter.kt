package dev.falcon.garagefinderpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchRecyclerAdapter : RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>()
{
//    private var names = arrayOf("Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9", "Title 10")
//    private var reach = arrayOf("Description 1", "Description 2", "Description 3", "Description 4", "Description 5", "Description 6", "Description 7", "Description 8", "Description 9", "Description 10")
//    private var images = arrayOf(R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz,R.drawable.valiantplayz)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.search_resource, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: SearchRecyclerAdapter.ViewHolder, position: Int) {
//        holder.name.text = names[position]
//        holder.description.text = reach[position]
//        holder.image.setImageResource(images[position])
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var name: TextView
        lateinit var description: TextView
        lateinit var image: ImageView

        init {
            name = itemView.findViewById(R.id.garageName)
            description = itemView.findViewById(R.id.garageReach)
            image = itemView.findViewById(R.id.garageImage)
        }
    }

}