package com.alihantaycu.elterminali.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
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
}