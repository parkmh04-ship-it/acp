package com.acp.merchant.application.port.input

import com.acp.merchant.domain.model.CheckoutSession
import com.acp.schema.checkout.CreateCheckoutSessionRequest

interface CheckoutUseCase {
    suspend fun createSession(request: CreateCheckoutSessionRequest): CheckoutSession
    suspend fun getSession(id: String): CheckoutSession?
    suspend fun completeSession(id: String): CheckoutSession
}
