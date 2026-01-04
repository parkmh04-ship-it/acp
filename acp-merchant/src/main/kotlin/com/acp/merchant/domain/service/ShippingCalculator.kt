package com.acp.merchant.domain.service

import com.acp.merchant.domain.model.Address
import com.acp.merchant.domain.model.CheckoutItem
import com.acp.merchant.domain.model.FulfillmentOption
import java.math.BigDecimal
import org.springframework.stereotype.Component

/**
 * 배송비 계산 도메인 서비스
 *
 * 주문 금액, 배송지 주소, 상품 무게 등을 고려하여 배송비를 계산합니다.
 */
@Component
class ShippingCalculator {

    companion object {
        // 무료 배송 기준 금액 (50,000원)
        private val FREE_SHIPPING_THRESHOLD = BigDecimal("50000")

        // 당일 배송 가능 우편번호 (서울/경기 일부)
        private val SAME_DAY_AVAILABLE_POSTAL_CODES =
                setOf(
                        "06",
                        "07",
                        "08", // 서울 강남/서초/송파
                        "13",
                        "14",
                        "15", // 경기 성남/용인
                )
    }

    /**
     * 주문에 대한 사용 가능한 배송 옵션 목록을 반환합니다.
     *
     * @param items 주문 상품 목록
     * @param address 배송지 주소
     * @param orderAmount 주문 금액 (배송비 제외)
     * @return 사용 가능한 배송 옵션 목록
     */
    fun getAvailableFulfillmentOptions(
            items: List<CheckoutItem>,
            address: Address?,
            orderAmount: BigDecimal
    ): List<FulfillmentOption> {
        val options = mutableListOf<FulfillmentOption>()

        // 표준 배송 (항상 사용 가능)
        options.add(FulfillmentOption.standard(FREE_SHIPPING_THRESHOLD, orderAmount))

        // 빠른 배송 (항상 사용 가능)
        options.add(FulfillmentOption.express())

        // 당일 배송 (지역 제한)
        if (isSameDayAvailable(address)) {
            options.add(FulfillmentOption.sameDay())
        }

        return options
    }

    /**
     * 선택된 배송 옵션의 배송비를 계산합니다.
     *
     * @param fulfillmentOptionId 선택된 배송 옵션 ID
     * @param orderAmount 주문 금액 (배송비 제외)
     * @param address 배송지 주소
     * @return 배송비
     */
    fun calculateShippingCost(
            fulfillmentOptionId: String,
            orderAmount: BigDecimal,
            address: Address?
    ): BigDecimal {
        return when (fulfillmentOptionId) {
            "standard" -> {
                // 50,000원 이상 무료 배송
                if (orderAmount >= FREE_SHIPPING_THRESHOLD) {
                    BigDecimal.ZERO
                } else {
                    BigDecimal("3000")
                }
            }
            "express" -> BigDecimal("5000")
            "same_day" -> {
                if (isSameDayAvailable(address)) {
                    BigDecimal("10000")
                } else {
                    throw IllegalArgumentException("당일 배송은 해당 지역에서 사용할 수 없습니다.")
                }
            }
            else -> throw IllegalArgumentException("알 수 없는 배송 옵션: $fulfillmentOptionId")
        }
    }

    /**
     * 당일 배송 가능 여부를 확인합니다.
     *
     * @param address 배송지 주소
     * @return 당일 배송 가능 여부
     */
    private fun isSameDayAvailable(address: Address?): Boolean {
        if (address == null || address.postalCode == null) {
            return false
        }

        // 한국 우편번호 형식: 12345 (5자리)
        // 앞 2자리로 지역 판단
        val postalCodePrefix = address.postalCode.take(2)

        return address.countryCode == "KR" &&
                SAME_DAY_AVAILABLE_POSTAL_CODES.contains(postalCodePrefix)
    }
}
