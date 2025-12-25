package com.acp.merchant.repository

import com.acp.merchant.generated.jooq.tables.references.PRODUCTS
import com.acp.merchant.generated.jooq.tables.pojos.Products
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Repository
class ProductRepository(
    private val dsl: DSLContext
) {
    suspend fun findAll(): List<Products> = withContext(Dispatchers.IO) {
        dsl.selectFrom(PRODUCTS)
            .fetchInto(Products::class.java)
    }

    suspend fun saveAll(products: List<Products>) = withContext(Dispatchers.IO) {
        val records = products.map { product ->
            dsl.newRecord(PRODUCTS, product)
        }
        dsl.batchInsert(records).execute()
    }
    
    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dsl.deleteFrom(PRODUCTS).execute()
    }
}
