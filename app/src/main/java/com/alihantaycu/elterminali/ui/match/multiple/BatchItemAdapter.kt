package com.alihantaycu.elterminali.ui.match.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ItemBatchBinding

data class BatchItem(
    val product: Product,
    val boxRfid: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BatchItemAdapter(
    private val onRemoveClick: (BatchItem) -> Unit
) : RecyclerView.Adapter<BatchItemAdapter.ViewHolder>() {

    private val items = mutableListOf<BatchItem>()

    inner class ViewHolder(private val binding: ItemBatchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BatchItem) {
            binding.apply {
                productName.text = item.product.name
                productInfo.text = "IMEI: ${item.product.imei}"
                boxInfo.text = "Kutu: ${item.boxRfid}"

                removeButton.setOnClickListener { onRemoveClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBatchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun addItem(item: BatchItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(item: BatchItem) {
        val position = items.indexOf(item)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getAllItems() = items.toList()

    fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}