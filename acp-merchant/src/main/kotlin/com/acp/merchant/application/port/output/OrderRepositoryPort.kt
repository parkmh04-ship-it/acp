package com.acp.merchant.application.port.output

import com.acp.merchant.domain.model.Order

interface OrderRepositoryPort {
    suspend fun save(order: Order): Order
    suspend fun findById(id: String): Order?
}
