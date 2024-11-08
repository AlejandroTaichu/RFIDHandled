package com.alihantaycu.elterminali.ui.warehouse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.databinding.ItemWarehouseBinding

data class Warehouse(val name: String, val description: String, val iconResId: Int)

class WarehouseAdapter(
    private var warehouses: List<Warehouse>,
    private val onWarehouseClick: (Warehouse) -> Unit
) : RecyclerView.Adapter<WarehouseAdapter.WarehouseViewHolder>() {

    class WarehouseViewHolder(private val binding: ItemWarehouseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(warehouse: Warehouse, onWarehouseClick: (Warehouse) -> Unit) {
            binding.apply {
                warehouseName.text = warehouse.name
                warehouseDescription.text = warehouse.description
                warehouseIcon.setImageResource(warehouse.iconResId)
                root.setOnClickListener { onWarehouseClick(warehouse) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseViewHolder {
        val binding = ItemWarehouseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarehouseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarehouseViewHolder, position: Int) {
        holder.bind(warehouses[position], onWarehouseClick)
    }

    override fun getItemCount() = warehouses.size

    fun updateWarehouses(newWarehouses: List<Warehouse>) {
        warehouses = newWarehouses
        notifyDataSetChanged()
    }
}