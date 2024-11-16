package com.alihantaycu.elterminali.data.dao

import androidx.room.*
import com.alihantaycu.elterminali.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE location = :location")
    fun getProductsByLocation(location: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE imei = :imei LIMIT 1")
    suspend fun getProductByImei(imei: String): Product?

    @Query("SELECT MAX(id) + 1 FROM products")
    suspend fun getNextId(): Int

    @Query("SELECT * FROM products WHERE imei = :imei LIMIT 1")
    suspend fun findByImei(imei: String): Product?

    @Query("SELECT * FROM products WHERE status = 'MATCHED'")
    fun getMatchedProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE matchedBoxRfid = :boxRfid")
    suspend fun getProductsByBoxRfid(boxRfid: String): List<Product>
}