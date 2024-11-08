package com.alihantaycu.elterminali.ui.count

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alihantaycu.elterminali.data.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountViewModel : ViewModel() {

    private val _scannedItems = MutableLiveData<List<Product>>()
    val scannedItems: LiveData<List<Product>> = _scannedItems

    private var isScanning = false

    fun startScanning(scanSimulator: () -> Product?) {
        if (isScanning) return
        isScanning = true
        viewModelScope.launch(Dispatchers.Default) {
            val currentItems = _scannedItems.value?.toMutableList() ?: mutableListOf()
            while (isScanning) {
                delay(1000) // 1 saniye bekle
                scanSimulator()?.let { product ->
                    if (!currentItems.contains(product)) {
                        currentItems.add(product)
                        _scannedItems.postValue(currentItems)
                    }
                }
            }
        }
    }

    fun stopScanning() {
        isScanning = false
    }
}