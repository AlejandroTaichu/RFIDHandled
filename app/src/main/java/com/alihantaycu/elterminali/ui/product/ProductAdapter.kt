package com.alihantaycu.elterminali.ui.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ItemProductManageBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit,
    private val onGenerateQRClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    private var filteredProducts: List<Product> = products

    class ProductViewHolder(private val binding: ItemProductManageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            product: Product,
            onItemClick: (Product) -> Unit,
            onEditClick: (Product) -> Unit,
            onDeleteClick: (Product) -> Unit,
            onGenerateQRClick: (Product) -> Unit
        ) {
            binding.apply {
                // Temel bilgiler
                productName.text = product.name
                productRfid.text = "RFID: ${product.rfidTag}"
                productImei.text = "IMEI: ${product.imei}"
                productLocation.text = "Konum: ${product.location}"
                productAddress.text = "Adres: ${product.address}"
                productCreatedDate.text = "Oluşturulma Tarihi: ${product.createdDate}"

                // Boş alanları gizle
                productImei.visibility = if (product.imei.isBlank()) View.GONE else View.VISIBLE
                productAddress.visibility = if (product.address.isBlank()) View.GONE else View.VISIBLE

                // Eşleştirme durumu gösterimi
                productStatus.text = when(product.status) {
                    "MATCHED" -> "Eşleştirildi"
                    "NEW" -> "Yeni"
                    else -> product.status
                }
                productStatus.visibility = View.VISIBLE

                // Eşleşen kutu bilgisi
                if (product.matchedBoxRfid != null) {
                    matchedBoxInfo.text = "Kutu: ${product.matchedBoxRfid}"
                    matchedBoxInfo.visibility = View.VISIBLE
                } else {
                    matchedBoxInfo.visibility = View.GONE
                }

                // Click listeners
                root.setOnClickListener { onItemClick(product) }
                editButton.setOnClickListener { onEditClick(product) }
                deleteButton.setOnClickListener { onDeleteClick(product) }
                generateQRButton.setOnClickListener { onGenerateQRClick(product) }
            }
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        filteredProducts = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(
            filteredProducts[position],
            onItemClick,
            onEditClick,
            onDeleteClick,
            onGenerateQRClick
        )
    }

    override fun getItemCount(): Int = filteredProducts.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<Product> = mutableListOf()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(products)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in products) {
                        if (item.name.lowercase().contains(filterPattern) ||
                            item.rfidTag.lowercase().contains(filterPattern) ||
                            item.imei.lowercase().contains(filterPattern) ||
                            item.location.lowercase().contains(filterPattern) ||
                            item.address.lowercase().contains(filterPattern) ||
                            (item.matchedBoxRfid?.lowercase()?.contains(filterPattern) == true)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredProducts = results?.values as? List<Product> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}