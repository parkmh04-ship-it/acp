package com.acp.merchant.application.service

import com.acp.merchant.application.port.input.CheckoutUseCase
import com.acp.merchant.application.port.output.CheckoutRepositoryPort
import com.acp.merchant.application.port.output.PaymentClient
import com.acp.merchant.application.port.output.ProductPersistencePort
import com.acp.merchant.domain.model.*
import com.acp.merchant.domain.service.PricingEngine
import com.acp.schema.checkout.CreateCheckoutSessionRequest
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentItem as PaymentRequestItem
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CheckoutService(
    private val checkoutRepository: CheckoutRepositoryPort,
    private val productRepository: ProductPersistencePort,
    private val paymentClient: PaymentClient,
    private val pricingEngine: PricingEngine
) : CheckoutUseCase {

    override suspend fun createSession(request: CreateCheckoutSessionRequest): CheckoutSession {
        // 1. Fetch products and validate
        val checkoutItems = request.items.map { requestItem ->
            val product = productRepository.findById(requestItem.id)
                ?: throw IllegalArgumentException("Product not found: ${requestItem.id}")
            
            // TODO: Check inventory availability
            
            val unitPrice = product.priceAmount ?: throw IllegalStateException("Product price missing")
            val totalPrice = unitPrice.multiply(java.math.BigDecimal(requestItem.quantity))
            
            CheckoutItem(
                productId = requestItem.id,
                quantity = requestItem.quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice
            )
        }
        
        // 2. Calculate Totals
        val totals = pricingEngine.calculateTotals(checkoutItems)
        
        // 3. Create Session
        val session = CheckoutSession(
            id = UUID.randomUUID().toString(),
            status = CheckoutStatus.NOT_READY,
            currency = "KRW", // Default for now, should come from products or request
            items = checkoutItems,
            buyer = request.buyer?.let { Buyer(it.email, it.name) },
            shippingAddress = request.fulfillmentAddress?.let { Address(it.countryCode, it.postal_code) },
            totals = totals
        )
        
        // 4. Save
        return checkoutRepository.save(session)
    }

    override suspend fun getSession(id: String): CheckoutSession? {
        return checkoutRepository.findById(id)
    }

    override suspend fun completeSession(id: String): CheckoutSession {
        val session = checkoutRepository.findById(id)
            ?: throw NoSuchElementException("Session not found: $id")

        if (session.status == CheckoutStatus.COMPLETED) {
             throw IllegalStateException("Session already completed")
        }
        
        if (!session.isReadyForPayment()) {
             throw IllegalStateException("Session not ready for payment. Missing buyer or shipping info.")
        }

        // Fetch product names (N+1 but acceptable for MVP)
        val paymentItems = session.items.map { item ->
            val product = productRepository.findById(item.productId)
            val name = product?.title ?: "Product ${item.productId}"
            
             PaymentRequestItem(
                 name = name,
                 quantity = item.quantity,
                 unitPrice = item.unitPrice.toLong(),
                 currency = session.currency
             )
        }

        // Call PSP
        val request = PaymentPrepareRequest(
            merchantOrderId = session.id,
            amount = session.totals.total.toLong(),
            currency = session.currency,
            items = paymentItems
        )

        val response = paymentClient.preparePayment(request)

        // Update session
        val updatedSession = session.copy(
            status = CheckoutStatus.READY,
            nextActionUrl = response.redirectUrl
        )
        
        return checkoutRepository.save(updatedSession)
    }
}