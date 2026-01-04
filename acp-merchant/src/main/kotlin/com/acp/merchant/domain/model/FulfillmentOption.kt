package com.acp.merchant.domain.model

import java.math.BigDecimal

/**
 * 배송 옵션 도메인 모델
 *
 * OpenAI Checkout Spec의 Fulfillment Options를 구현합니다. 각 배송 옵션은 배송 방법, 예상 배송 기간, 배송비를 포함합니다.
 */
data class FulfillmentOption(
        val id: String,
        val name: String,
        val description: String,
        val estimatedMinDays: Int,
        val estimatedMaxDays: Int,
        val cost: BigDecimal,
        val currency: String = "KRW"
) {
    companion object {
        /** 표준 배송 (3-5일, 무료 또는 3,000원) */
        fun standard(
                freeShippingThreshold: BigDecimal,
                orderAmount: BigDecimal
        ): FulfillmentOption {
            val cost =
                    if (orderAmount >= freeShippingThreshold) {
                        BigDecimal.ZERO
                    } else {
                        BigDecimal("3000")
                    }

            return FulfillmentOption(
                    id = "standard",
                    name = "표준 배송",
                    description = "일반 택배 배송 (${freeShippingThreshold.toPlainString()}원 이상 무료)",
                    estimatedMinDays = 3,
                    estimatedMaxDays = 5,
                    cost = cost
            )
        }

        /** 빠른 배송 (1-2일, 5,000원) */
        fun express(): FulfillmentOption {
            return FulfillmentOption(
                    id = "express",
                    name = "빠른 배송",
                    description = "익일 또는 당일+1 배송",
                    estimatedMinDays = 1,
                    estimatedMaxDays = 2,
                    cost = BigDecimal("5000")
            )
        }

        /** 당일 배송 (0-1일, 10,000원, 지역 제한) */
        fun sameDay(): FulfillmentOption {
            return FulfillmentOption(
                    id = "same_day",
                    name = "당일 배송",
                    description = "오늘 주문 시 오늘 도착 (서울/경기 일부 지역)",
                    estimatedMinDays = 0,
                    estimatedMaxDays = 1,
                    cost = BigDecimal("10000")
            )
        }

        /** 기본 배송 옵션 목록 반환 */
        fun getDefaultOptions(
                orderAmount: BigDecimal,
                freeShippingThreshold: BigDecimal = BigDecimal("50000")
        ): List<FulfillmentOption> {
            return listOf(standard(freeShippingThreshold, orderAmount), express(), sameDay())
        }
    }
}
