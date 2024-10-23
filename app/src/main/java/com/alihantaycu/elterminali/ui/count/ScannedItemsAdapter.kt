package com.alihantaycu.elterminali.ui.count

import android.view.LayoutInflater
<<<<<<< HEAD
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.model.Product
import com.alihantaycu.elterminali.databinding.ItemScannedProductBinding

class ScannedItemsAdapter(private val scannedItems: List<Product>) :
    RecyclerView.Adapter<ScannedItemsAdapter.ScannedItemViewHolder>() {

    class ScannedItemViewHolder(private val binding: ItemScannedProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                productName.text = product.name
                productRfid.text = "RFID: ${product.rfidTag}"
                productImei.text = "IMEI: ${product.imei}"
                productLocation.text = "Depo: ${product.location}"
                productAddress.text = "Konum: ${product.address}"
                productCreatedDate.text = "Tarih: ${product.createdDate}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemViewHolder {
        val binding = ItemScannedProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScannedItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScannedItemViewHolder, position: Int) {
        holder.bind(scannedItems[position])
    }

    override fun getItemCount() = scannedItems.size
=======
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
>>>>>>> Demo2
}