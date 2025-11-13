package com.example.myexpirationdate.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myexpirationdate.data.DataSource
import com.example.myexpirationdate.data.ProductDao
import com.example.myexpirationdate.medApp
import com.example.myexpirationdate.models.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Step 10: Define ProductState to pass to Views
data class ProductState(
    var id: String = "",
    var name: String = "",
    var quantity: String = "",
    var price: String = "",
){
    fun toProduct() : Product {
        return Product(
            product_id = id.toIntOrNull() ?: 1,
            name = name,
            quantity = quantity.toIntOrNull() ?: 0,
            unit_price = price.toDoubleOrNull() ?: 0.0
        )
    }
}


//// Step 7: Dependency Injection and create a singleton
class RoomVM(
    val productDao : ProductDao,
) : ViewModel(){

    // Step 8: Import data to Product Table and show them
    init {
        importDataToProductTable()
    }

    private fun importDataToProductTable(){
        viewModelScope.launch {
            DataSource.listOfProducts.forEach { product ->
                productDao.upsertProduct(product)
            }
        }
    }


    //    // Step 10: Enable Upsert, create a state variable to handle User input
    var product_state by mutableStateOf(ProductState())

    fun updateProductId(id: String){
        product_state = product_state.copy(id = id)
    }

    fun updateProductName(name: String){
        product_state = product_state.copy(name = name)
    }

    fun updateProductQuantity(quantity: String){
        product_state = product_state.copy(quantity = quantity)
    }

    fun updateProductPrice(price: String){
        product_state = product_state.copy(price = price)
    }


    // Step 8: show the list of Products
    //val products = productDao.getAllProduct()
    val products = productDao.getProductWithIds(arrayOf(2,3))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Step 9: Enable Delete
    fun deleteProduct(product: Product){
        viewModelScope.launch {
            productDao.deleteProduct(product)
        }
    }

    //    // Step 10 Enable Upsert to add a new product to the Product Table
    fun addProduct(){
        val id = product_state.id.toIntOrNull()
        if(id != null) {
            viewModelScope.launch {
                productDao.upsertProduct(product_state.toProduct())
            }
        }
    }

    // Step 7: Create a singleton of the VM and pass the parameters to it from App Container
    companion object{
        private var INSTANCE : RoomVM? = null

        fun getInstance() : RoomVM{
            val vm = INSTANCE ?: synchronized(this){
                RoomVM(
                    medApp.Companion.getApp().container.productDao,
                ).also {
                    INSTANCE = it
                }
            }
            return vm
        }
    }

}

