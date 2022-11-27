package com.example.zadanie.ui.widget.barlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getDrawable
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.helpers.autoNotify
import com.example.zadanie.ui.fragments.BarsFragmentDirections
import com.google.android.material.chip.Chip
import kotlin.properties.Delegates

class BarsAdapter(val events: BarsEvents? = null) :
    RecyclerView.Adapter<BarsAdapter.BarItemViewHolder>() {
    var items: List<BarItem> by Delegates.observable(emptyList()) { _, old, new ->
        autoNotify(old, new) { o, n -> o.id.compareTo(n.id) == 0 }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarItemViewHolder {
        return BarItemViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BarItemViewHolder, position: Int) {
        holder.bind(items[position], events)
    }

    class BarItemViewHolder(
        private val parent: ViewGroup,
        itemView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.bar_item,
            parent,
            false)
        ) : RecyclerView.ViewHolder(itemView){

        fun bind(item: BarItem, events: BarsEvents?) {
            itemView.findViewById<TextView>(R.id.name).text = item.name
            itemView.findViewById<TextView>(R.id.count).text = item.users.toString()
            itemView.findViewById<Chip>(R.id.type).text = item.type
                    .replace("node", "place")
                    .replace('_', ' ')

            val drawable : Int = when (item.type){
                "cafe" -> {
                    R.drawable.ic_baseline_local_cafe_24
                }
                "restaurant" -> {
                    R.drawable.ic_baseline_restaurant_24
                }
                "pub" -> {
                    R.drawable.ic_baseline_sports_bar_24
                }
                "fast_food" -> {
                    R.drawable.ic_baseline_fastfood_24
                }
                "bar" -> {
                    R.drawable.ic_baseline_local_bar_24
                }
                "nightclub" -> {
                    R.drawable.ic_baseline_nightlife_24
                }
                "stripclub" -> {
                    R.drawable.ic_baseline_wine_bar_24
                }
                else -> {
                    R.drawable.ic_baseline_local_drink_24
                }
            }

            itemView.findViewById<Chip>(R.id.type).chipIcon = getDrawable(parent.context, drawable)

            itemView.setOnClickListener { events?.onBarClick(item) }
        }
    }
}