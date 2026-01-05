package com.acp.merchant.adapter.inbound.web

import com.acp.merchant.application.port.input.CheckoutUseCase
import com.acp.merchant.domain.model.CheckoutSession
import com.acp.schema.checkout.*
import com.acp.schema.checkout.CheckoutStatus as DtoCheckoutStatus
import com.acp.merchant.domain.model.CheckoutStatus as DomainCheckoutStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/checkout_sessions")
class CheckoutController(
    private val checkoutUseCase: CheckoutUseCase
) {

    @PostMapping
    suspend fun createSession(@RequestBody request: CreateCheckoutSessionRequest): CheckoutSessionResponse {
        val session = checkoutUseCase.createSession(request)
        return session.toDto()
    }

    @GetMapping("/{id}")
    suspend fun getSession(@PathVariable id: String): CheckoutSessionResponse {
        val session = checkoutUseCase.getSession(id)
            ?: throw NoSuchElementException("Session not found")
        return session.toDto()
    }

    @PostMapping("/{id}")
    suspend fun updateSession(@PathVariable id: String, @RequestBody request: UpdateCheckoutSessionRequest): CheckoutSessionResponse {
        val session = checkoutUseCase.updateSession(id, request)
        return session.toDto()
    }

    @PostMapping("/{id}/complete")
    suspend fun completeSession(@PathVariable id: String): CheckoutSessionResponse {
        val session = checkoutUseCase.completeSession(id)
        return session.toDto()
    }

    @PostMapping("/{id}/confirm")
    suspend fun confirmPayment(@PathVariable id: String, @RequestBody request: Map<String, String>): CheckoutSessionResponse {
        val pgToken = request["pg_token"] ?: throw IllegalArgumentException("pg_token is required")
        val session = checkoutUseCase.confirmPayment(id, pgToken)
        return session.toDto()
    }

    @PostMapping("/{id}/cancel")
    suspend fun cancelSession(@PathVariable id: String, @RequestBody(required = false) request: Map<String, String>?): CheckoutSessionResponse {
        val reason = request?.get("reason") ?: "User requested cancellation"
        val session = checkoutUseCase.cancelSession(id, reason)
        return session.toDto()
    }

    private fun CheckoutSession.toDto(): CheckoutSessionResponse {
        return CheckoutSessionResponse(
            id = this.id,
            paymentProvider = PaymentProvider(provider = "kakaopay"),
            status = when (this.status) {
                DomainCheckoutStatus.NOT_READY -> DtoCheckoutStatus.NOT_READY
                DomainCheckoutStatus.READY -> DtoCheckoutStatus.READY
                DomainCheckoutStatus.COMPLETED -> DtoCheckoutStatus.COMPLETED
                DomainCheckoutStatus.CANCELED -> DtoCheckoutStatus.CANCELED
            },
            currency = this.currency,
            lineItems = this.items.map { item ->
                LineItem(
                    id = item.productId,
                    item = com.acp.schema.checkout.CheckoutItem(
                        id = item.productId,
                        quantity = item.quantity
                    ),
                    baseAmount = item.totalPrice.toLong(),
                    subtotal = item.totalPrice.toLong(),
                    tax = 0,
                    total = item.totalPrice.toLong()
                )
            },
            fulfillmentOptions = this.availableFulfillmentOptions.map { option ->
                com.acp.schema.checkout.FulfillmentOption(
                    id = option.id,
                    name = option.name,
                    description = option.description,
                    estimatedMinDays = option.estimatedMinDays,
                    estimatedMaxDays = option.estimatedMaxDays,
                    amount = option.cost.toLong(),
                    currency = option.currency
                )
            },
            totals = listOfNotNull(
                Total(TotalType.ITEMS_BASE_AMOUNT, "상품 금액", this.totals.itemsBaseAmount.toLong()),
                Total(TotalType.SUBTOTAL, "소계", this.totals.subtotal.toLong()),
                Total(TotalType.TAX, "부가세", this.totals.tax.toLong()),
                if (this.selectedFulfillmentOption != null) {
                    val optionName = this.availableFulfillmentOptions.find { it.id == this.selectedFulfillmentOption }?.name ?: "배송비"
                    Total(TotalType.FULFILLMENT, optionName, this.totals.shipping.toLong())
                } else if (this.availableFulfillmentOptions.isNotEmpty()) {
                    Total(TotalType.FULFILLMENT, "배송비 (미선택)", 0)
                } else null,
                Total(TotalType.TOTAL, "총 결제 금액", this.totals.total.toLong())
            ),
            messages = emptyList(),
            links = emptyList(),
            nextActionUrl = this.nextActionUrl
        )
    }
}
