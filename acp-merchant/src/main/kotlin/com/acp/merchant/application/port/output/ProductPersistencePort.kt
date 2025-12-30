package com.acp.merchant.application.port.output

import com.acp.merchant.generated.jooq.tables.pojos.Products

/** 상품 영속성 포트 (Output Port) */
interface ProductPersistencePort {
    suspend fun findAll(): List<Products>
    suspend fun findById(id: String): Products?
    suspend fun saveAll(products: List<Products>)
    suspend fun deleteAll()
}
