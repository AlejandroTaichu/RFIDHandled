package com.alihantaycu.elterminali.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
<<<<<<< HEAD
import com.alihantaycu.elterminali.data.model.Product
import com.alihantaycu.elterminali.databinding.ItemInventoryBinding
import com.alihantaycu.elterminali.ui.common.BaseAdapter

class InventoryAdapter(products: List<Product>) : BaseAdapter<Product, ItemInventoryBinding>(products) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemInventoryBinding {
        return ItemInventoryBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemInventoryBinding, item: Product) {
        binding.apply {
            productName.text = item.name
            productRfid.text = "RFID: ${item.rfidTag}"
            productImei.text = "IMEI: ${item.imei}"
            productLocation.text = "Depo: ${item.location}"
            productAddress.text = "Konum: ${item.address}"
            productCreatedDate.text = "Oluşturulma: ${item.createdDate}"
        }
    }
=======
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ItemInventoryBinding

class InventoryAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                productName.text = product.name
                productRfid.text = "RFID: ${product.rfidTag}"
                productImei.text = "IMEI: ${product.imei}"
                productLocation.text = "Depo: ${product.location}"
                productAddress.text = "Konum: ${product.address}"
                productCreatedDate.text = "Oluşturulma: ${product.createdDate}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
>>>>>>> Demo2
}