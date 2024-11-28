package com.alihantaycu.elterminali.ui.count

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product

class ScannedItemsAdapter : RecyclerView.Adapter<ScannedItemsAdapter.ViewHolder>() {

    private val items = LinkedHashMap<String, Product>() // RFID -> Product
    private val countMap = mutableMapOf<String, Int>() // RFID -> Count

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val rfid: TextView = view.findViewById(R.id.rfid)
        val imei: TextView = view.findViewById(R.id.imei)
        val location: TextView = view.findViewById(R.id.location)
        val count: TextView = view.findViewById(R.id.count)
    }

    fun clear() {
        items.clear()
        countMap.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanned_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items.values.elementAt(position)
        holder.productName.text = product.name
        holder.rfid.text = product.rfidTag
        holder.imei.text = product.imei
        holder.location.text = product.location
        holder.count.text = countMap[product.rfidTag].toString()
    }

    override fun getItemCount() = items.size

    fun addItem(product: Product) {
        val rfid = product.rfidTag
        if (!items.containsKey(rfid)) {
            items[rfid] = product
            countMap[rfid] = 1
        } else {
            countMap[rfid] = (countMap[rfid] ?: 0) + 1
        }
        notifyDataSetChanged()
    }
}