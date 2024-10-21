package com.alihantaycu.elterminali.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ItemProductManageBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(private val binding: ItemProductManageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            product: Product,
            onItemClick: (Product) -> Unit,
            onEditClick: (Product) -> Unit,
            onDeleteClick: (Product) -> Unit
        ) {
            binding.apply {
                productName.text = product.name
                productRfid.text = "RFID: ${product.rfidTag}"
                productImei.text = "IMEI: ${product.imei}"
                productLocation.text = "Konum: ${product.location}"
                productAddress.text = "Adres: ${product.address}"
                productCreatedDate.text = "Oluşturulma Tarihi: ${product.createdDate}"

                root.setOnClickListener { onItemClick(product) }
                editButton.setOnClickListener { onEditClick(product) }
                deleteButton.setOnClickListener { onDeleteClick(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], onItemClick, onEditClick, onDeleteClick)
    }

    override fun getItemCount() = products.size
}