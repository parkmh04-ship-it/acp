package com.acp.merchant.application.service

import com.acp.merchant.application.port.input.ProductAdminUseCase
import com.acp.merchant.application.port.output.ProductPersistencePort
import com.acp.merchant.generated.jooq.tables.pojos.Products
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/** 상품 관리 서비스 (Application Service) */
@Service
class ProductAdminService(private val productPersistencePort: ProductPersistencePort) :
        ProductAdminUseCase {

    override suspend fun initMockData() {
        logger.info { "Initializing Mock Product Data" }
        productPersistencePort.deleteAll()

        val mockProducts =
                listOf(
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
                                description =
                                        "Noise-cancelling wireless headphones with 20h battery life.",
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

        productPersistencePort.saveAll(mockProducts)
        logger.info { "Mock Product Data Initialized Successfully" }
    }
}
