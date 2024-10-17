package com.alihantaycu.elterminali.ui.warehouse

import android.view.LayoutInflater
import android.view.ViewGroup
import com.alihantaycu.elterminali.databinding.ItemWarehouseBinding
import com.alihantaycu.elterminali.ui.common.BaseAdapter

data class Warehouse(val name: String, val description: String, val iconResId: Int)

class WarehouseAdapter(
    warehouses: List<Warehouse>,
    private val onWarehouseClick: (Warehouse) -> Unit
) : BaseAdapter<Warehouse, ItemWarehouseBinding>(warehouses) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemWarehouseBinding {
        return ItemWarehouseBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemWarehouseBinding, item: Warehouse) {
        binding.apply {
            warehouseName.text = item.name
            warehouseDescription.text = item.description
            warehouseIcon.setImageResource(item.iconResId)
            root.setOnClickListener { onWarehouseClick(item) }
        }
    }
}