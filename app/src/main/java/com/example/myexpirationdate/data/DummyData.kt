package com.example.myexpirationdate.data
import com.example.myexpirationdate.models.Product

object DataSource  {
    val listOfProducts = listOf<Product>(

        Product(1, "Foam Dinner Plate", 70, 1.21),
        Product(2, "Beef Steak", 50, 12.99),
        Product(3, "Petit Baguette", 25, 10.99),
        Product(4, "Longan", 5, 1.99)

    )
}