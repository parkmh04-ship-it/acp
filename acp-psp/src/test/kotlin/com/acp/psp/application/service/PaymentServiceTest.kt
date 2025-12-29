package com.acp.psp.application.service

import com.acp.psp.application.port.output.*
import com.acp.psp.generated.jooq.tables.pojos.Payments
import com.acp.schema.payment.PaymentItem
import com.acp.schema.payment.PaymentPrepareRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** PaymentService 단위 테스트 */
class PaymentServiceTest {

    private val paymentRepositoryPort = mockk<PaymentRepositoryPort>(relaxed = true)
    private val paymentProvider = mockk<PaymentProvider>()
    private val paymentService = PaymentService(paymentRepositoryPort, paymentProvider)

    @Test
    @DisplayName("기존 결제 기록이 없는 경우, 새 결제를 준비하고 저장해야 한다")
    fun `preparePayment signs new payment when no existing record found`() = runTest {
        // Given
        val request =
                PaymentPrepareRequest(
                        merchantOrderId = "ORDER-123",
                        amount = 10000L,
                        items = listOf(PaymentItem("테스트 상품", 1, 10000L))
                )
        val mockPrepareResult =
                PaymentPrepareResult(
                        pgTid = "TID-ABC-123",
                        redirectUrl = "https://kakaopay.com/pay/abc123"
                )

        coEvery { paymentRepositoryPort.findByMerchantOrderId("ORDER-123") } returns null
        coEvery { paymentProvider.prepare(any()) } returns mockPrepareResult

        // When
        val response = paymentService.preparePayment(request)

        // Then
        assertNotNull(response.paymentId)
        assertEquals("ORDER-123", response.merchantOrderId)
        assertEquals(mockPrepareResult.redirectUrl, response.redirectUrl)
        assertEquals("READY", response.status)

        coVerify(exactly = 1) { paymentProvider.prepare(request) }
        coVerify(exactly = 1) { paymentRepositoryPort.save(any()) }
    }

    @Test
    @DisplayName("이미 동일한 주문번호의 결제가 존재하는 경우, 기존 정보를 반환해야 한다 (멱등성)")
    fun `preparePayment returns existing record when already exists`() = runTest {
        // Given
        val request =
                PaymentPrepareRequest(
                        merchantOrderId = "ORDER-123",
                        amount = 10000L,
                        items = listOf(PaymentItem("테스트 상품", 1, 10000L))
                )
        val existingPayment =
                Payments(
                        id = "EXISTING-PAY-ID",
                        merchantOrderId = "ORDER-123",
                        amount = BigDecimal.valueOf(10000L),
                        currency = "KRW",
                        status = "READY",
                        pgTid = "TID-PREVIOUS"
                )

        coEvery { paymentRepositoryPort.findByMerchantOrderId("ORDER-123") } returns existingPayment

        // When
        val response = paymentService.preparePayment(request)

        // Then
        assertEquals("EXISTING-PAY-ID", response.paymentId)
        assertEquals("ORDER-123", response.merchantOrderId)
        assertEquals("READY", response.status)

        // PG사 준비 API는 호출되지 않아야 함
        coVerify(exactly = 0) { paymentProvider.prepare(any()) }
        // 추가 저장도 일어나지 않아야 함 (이미 존재하므로)
        coVerify(exactly = 0) { paymentRepositoryPort.save(any()) }
    }
}
