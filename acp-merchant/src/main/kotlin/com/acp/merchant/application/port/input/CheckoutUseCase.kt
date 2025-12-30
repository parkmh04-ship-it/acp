package com.acp.merchant.application.port.input

import com.acp.merchant.domain.model.CheckoutSession
import com.acp.schema.checkout.CreateCheckoutSessionRequest
import com.acp.schema.checkout.UpdateCheckoutSessionRequest

interface CheckoutUseCase {
    suspend fun createSession(request: CreateCheckoutSessionRequest): CheckoutSession
    suspend fun getSession(id: String): CheckoutSession?
    suspend fun updateSession(id: String, request: UpdateCheckoutSessionRequest): CheckoutSession
    suspend fun completeSession(id: String): CheckoutSession
}
