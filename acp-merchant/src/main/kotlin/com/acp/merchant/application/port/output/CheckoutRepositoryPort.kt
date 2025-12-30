package com.acp.merchant.application.port.output

import com.acp.merchant.domain.model.CheckoutSession

interface CheckoutRepositoryPort {
    suspend fun save(checkoutSession: CheckoutSession): CheckoutSession
    suspend fun findById(id: String): CheckoutSession?
}
