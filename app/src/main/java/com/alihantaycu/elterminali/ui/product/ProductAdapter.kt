package com.alihantaycu.elterminali.ui.product

import android.view.LayoutInflater
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
    private val onGenerateQRClick: (Product) -> Unit  // QR kod oluşturma için yeni callback
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    private var filteredProducts: List<Product> = products

    class ProductViewHolder(private val binding: ItemProductManageBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(
            product: Product,
            onItemClick: (Product) -> Unit,
            onEditClick: (Product) -> Unit,
            onDeleteClick: (Product) -> Unit,
            onGenerateQRClick: (Product) -> Unit
        ) {
            binding.apply {
                productName.text = product.name
                productRfid.text = "RFID: ${product.rfidTag}"
                productImei.text = "IMEI: ${product.imei}"
                productLocation.text = "Konum: ${product.location}"
                productAddress.text = "Adres: ${product.address}"
                productCreatedDate.text = "Oluşturulma Tarihi: ${product.createdDate}"

                if (product.imei.isBlank()) productImei.visibility = android.view.View.GONE
                else productImei.visibility = android.view.View.VISIBLE

                if (product.address.isBlank()) productAddress.visibility = android.view.View.GONE
                else productAddress.visibility = android.view.View.VISIBLE

                root.setOnClickListener { onItemClick(product) }
                editButton.setOnClickListener { onEditClick(product) }
                deleteButton.setOnClickListener { onDeleteClick(product) }
                generateQRButton.setOnClickListener { onGenerateQRClick(product) }

                root.setOnClickListener { onItemClick(product) }
                editButton.setOnClickListener { onEditClick(product) }
                deleteButton.setOnClickListener { onDeleteClick(product) }
                generateQRButton.setOnClickListener { onGenerateQRClick(product) } // Yeni QR butonu için click listener
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

    // Filterable arayüzünü implement ediyoruz
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<Product> = mutableListOf()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(products)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in products) {
                        // Tüm alanlarda arama yap
                        if (item.name.lowercase().contains(filterPattern) ||  // İsim
                            item.rfidTag.lowercase().contains(filterPattern) ||  // RFID
                            item.imei.lowercase().contains(filterPattern) ||     // IMEI
                            item.location.lowercase().contains(filterPattern) || // Konum
                            item.address.lowercase().contains(filterPattern)) {  // Adres
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
