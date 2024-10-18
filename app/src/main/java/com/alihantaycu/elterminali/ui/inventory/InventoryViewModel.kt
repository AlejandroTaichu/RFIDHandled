package com.alihantaycu.elterminali.ui.inventory

import android.app.Application
import androidx.lifecycle.*
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val _allProducts = MediatorLiveData<List<Product>>()
    val allProducts: LiveData<List<Product>> = _allProducts

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
        _allProducts.addSource(repository.getAllProducts().asLiveData()) { products ->
            _allProducts.value = products
        }
    }

    fun getProductsByLocation(location: String): LiveData<List<Product>> {
        return repository.getProductsByLocation(location).asLiveData()
    }

    fun insertProduct(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteProduct(product)
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _allProducts.value = repository.getAllProducts().asLiveData().value
        }
    }
}