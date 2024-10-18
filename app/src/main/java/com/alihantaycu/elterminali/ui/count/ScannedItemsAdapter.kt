package com.alihantaycu.elterminali.ui.count

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.entity.Product
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
}