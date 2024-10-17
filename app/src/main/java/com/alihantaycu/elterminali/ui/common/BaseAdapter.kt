package com.alihantaycu.elterminali.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, VB : ViewBinding>(
    protected val items: List<T>
) : RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = createBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bind(holder.binding, items[position])
    }

    override fun getItemCount() = items.size

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun bind(binding: VB, item: T)
}