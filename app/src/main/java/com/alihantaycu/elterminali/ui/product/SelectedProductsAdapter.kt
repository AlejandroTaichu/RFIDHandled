package com.alihantaycu.elterminali.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ItemSelectedProductBinding  // Bunu oluşturacağız

class SelectedProductsAdapter(
    private var products: List<Product>,
    private val onRemoveClick: (Product) -> Unit
) : RecyclerView.Adapter<SelectedProductsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSelectedProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                productInfo.text = "${product.name} (${product.imei})"
                removeButton.setOnClickListener { onRemoveClick(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}