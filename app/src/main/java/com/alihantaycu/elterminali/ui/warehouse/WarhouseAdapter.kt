package com.alihantaycu.elterminali.ui.warehouse

import android.view.LayoutInflater
import android.view.ViewGroup
<<<<<<< HEAD
import com.alihantaycu.elterminali.databinding.ItemWarehouseBinding
import com.alihantaycu.elterminali.ui.common.BaseAdapter
=======
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.databinding.ItemWarehouseBinding
>>>>>>> Demo2

data class Warehouse(val name: String, val description: String, val iconResId: Int)

class WarehouseAdapter(
<<<<<<< HEAD
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
=======
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
>>>>>>> Demo2
}