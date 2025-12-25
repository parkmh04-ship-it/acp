package com.acp.merchant.service

import com.acp.merchant.generated.jooq.tables.pojos.Products
import com.acp.merchant.repository.ProductRepository
import com.acp.schema.feed.Availability
import com.acp.schema.feed.Condition
import com.acp.schema.feed.ProductFeedItem
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    suspend fun getProductFeed(): List<ProductFeedItem> {
        return productRepository.findAll().map { product ->
            ProductFeedItem(
                id = product.id!!,
                title = product.title!!,
                description = product.description ?: "",
                link = product.link!!,
                imageLink = product.imageLink!!,
                price = "${product.priceAmount} ${product.currency}",
                availability = Availability.valueOf(product.availability!!.uppercase()),
                productCategory = product.category,
                brand = product.brand,
                condition = try { Condition.valueOf(product.condition!!.uppercase()) } catch (e: Exception) { Condition.NEW },
                sellerName = "ACP Demo Shop"
            )
        }
    }

    suspend fun initMockData() {
        productRepository.deleteAll()
        
        val mockProducts = listOf(
            Products(
                id = "PROD-001",
                title = "Classic White T-Shirt",
                description = "A comfortable, premium cotton t-shirt.",
                link = "https://acp-merchant.com/products/001",
                imageLink = "https://acp-merchant.com/images/001.jpg",
                priceAmount = BigDecimal("25000"),
                currency = "KRW",
                availability = "IN_STOCK",
                inventoryQty = 100,
                condition = "new",
                category = "Apparel",
                brand = "ACP Basics"
            ),
            Products(
                id = "PROD-002",
                title = "Slim Fit Jeans",
                description = "Stylish dark blue jeans.",
                link = "https://acp-merchant.com/products/002",
                imageLink = "https://acp-merchant.com/images/002.jpg",
                priceAmount = BigDecimal("59000"),
                currency = "KRW",
                availability = "IN_STOCK",
                inventoryQty = 50,
                condition = "new",
                category = "Apparel",
                brand = "ACP Denim"
            ),
             Products(
                id = "PROD-003",
                title = "Wireless Headphones",
                description = "Noise-cancelling wireless headphones with 20h battery life.",
                link = "https://acp-merchant.com/products/003",
                imageLink = "https://acp-merchant.com/images/003.jpg",
                priceAmount = BigDecimal("129000"),
                currency = "KRW",
                availability = "IN_STOCK",
                inventoryQty = 20,
                condition = "new",
                category = "Electronics",
                brand = "ACP Audio"
            )
        )
        
        productRepository.saveAll(mockProducts)
    }
}
