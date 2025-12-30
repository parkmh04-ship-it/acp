package com.acp.merchant.domain.service

import com.acp.merchant.domain.model.CheckoutItem
import com.acp.merchant.domain.model.Totals
import java.math.BigDecimal
import org.springframework.stereotype.Component

@Component
class PricingEngine {
    // Simplified tax rate for MVP (10% VAT)
    private val TAX_RATE_KR = BigDecimal("0.10")

    fun calculateTotals(items: List<CheckoutItem>, shippingCost: BigDecimal = BigDecimal.ZERO): Totals {
        val itemsBaseAmount = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalPrice) }
        val itemsDiscount = BigDecimal.ZERO // TODO: Implement discounts
        val subtotal = itemsBaseAmount.subtract(itemsDiscount)
        
        // Simple tax calculation: Subtotal * TaxRate
        val tax = subtotal.multiply(TAX_RATE_KR)
        
        val total = subtotal.add(shippingCost).add(tax)

        return Totals(
            itemsBaseAmount = itemsBaseAmount,
            itemsDiscount = itemsDiscount,
            subtotal = subtotal,
            tax = tax,
            shipping = shippingCost,
            total = total
        )
    }
}
