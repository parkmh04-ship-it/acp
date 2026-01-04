package com.acp.merchant.domain.service

import com.acp.merchant.domain.model.Address
import org.springframework.stereotype.Component

/**
 * 주소 유효성 검증 도메인 서비스
 *
 * 배송지 주소의 형식과 배송 가능 여부를 검증합니다.
 */
@Component
class AddressValidator {

    companion object {
        // 지원하는 국가 코드 (ISO 3166-1 alpha-2)
        private val SUPPORTED_COUNTRIES = setOf("KR", "US", "JP")

        // 한국 우편번호 정규식 (5자리 숫자)
        private val KR_POSTAL_CODE_REGEX = Regex("^\\d{5}$")

        // 미국 우편번호 정규식 (12345 또는 12345-6789)
        private val US_POSTAL_CODE_REGEX = Regex("^\\d{5}(-\\d{4})?$")

        // 일본 우편번호 정규식 (123-4567)
        private val JP_POSTAL_CODE_REGEX = Regex("^\\d{3}-\\d{4}$")
    }

    /**
     * 주소 유효성을 검증합니다.
     *
     * @param address 검증할 주소
     * @return 검증 결과 (성공 시 null, 실패 시 에러 메시지)
     */
    fun validate(address: Address): ValidationResult {
        // 국가 코드 검증
        if (!SUPPORTED_COUNTRIES.contains(address.countryCode)) {
            return ValidationResult.failure("지원하지 않는 국가입니다: ${address.countryCode}")
        }

        // 우편번호 검증
        if (address.postalCode == null) {
            return ValidationResult.failure("우편번호는 필수입니다.")
        }

        val postalCodeValid =
                when (address.countryCode) {
                    "KR" -> KR_POSTAL_CODE_REGEX.matches(address.postalCode)
                    "US" -> US_POSTAL_CODE_REGEX.matches(address.postalCode)
                    "JP" -> JP_POSTAL_CODE_REGEX.matches(address.postalCode)
                    else -> false
                }

        if (!postalCodeValid) {
            return ValidationResult.failure("유효하지 않은 우편번호 형식입니다: ${address.postalCode}")
        }

        return ValidationResult.success()
    }

    /**
     * 배송 가능 여부를 확인합니다.
     *
     * @param address 확인할 주소
     * @return 배송 가능 여부
     */
    fun isDeliverable(address: Address): Boolean {
        // 현재는 모든 지원 국가에 배송 가능
        // 향후 특정 지역 제외 로직 추가 가능
        return SUPPORTED_COUNTRIES.contains(address.countryCode)
    }
}

/** 검증 결과 값 객체 */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null) {
    companion object {
        fun success() = ValidationResult(true, null)
        fun failure(message: String) = ValidationResult(false, message)
    }
}
