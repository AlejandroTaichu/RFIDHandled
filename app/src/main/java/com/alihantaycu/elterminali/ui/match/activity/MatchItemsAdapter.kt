package com.alihantaycu.elterminali.ui.match.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product

class MatchedItemsAdapter(
    private val onRemoveClick: (Product) -> Unit
) : RecyclerView.Adapter<MatchedItemsAdapter.ViewHolder>() {

    private val items = mutableListOf<Product>()
    fun updateItems(newItems: List<Product>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val rfid: TextView = view.findViewById(R.id.rfid)
        val imei: TextView = view.findViewById(R.id.imei)
        val removeButton: ImageButton = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matched_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.productName.text = item.name
        holder.rfid.text = "RFID: ${item.rfidTag}"
        holder.imei.text = "IMEI: ${item.imei}"

        holder.removeButton.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun addItem(product: Product) {
        items.add(product)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(product: Product) {
        val position = items.indexOf(product)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearItems() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getItems(): List<Product> = items.toList()
}