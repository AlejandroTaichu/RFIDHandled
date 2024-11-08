package com.alihantaycu.elterminali.ui.count

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product

class ScannedItemsAdapter : RecyclerView.Adapter<ScannedItemsAdapter.ViewHolder>() {

    private val items = mutableListOf<Product>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val rfid: TextView = view.findViewById(R.id.rfid)
        val imei: TextView = view.findViewById(R.id.imei)
        val location: TextView = view.findViewById(R.id.location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanned_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.productName.text = item.name
        holder.rfid.text = item.rfidTag
        holder.imei.text = item.imei
        holder.location.text = item.location
    }

    override fun getItemCount() = items.size

    fun addItem(product: Product) {
        items.add(product)
        notifyItemInserted(items.size - 1)
    }
}