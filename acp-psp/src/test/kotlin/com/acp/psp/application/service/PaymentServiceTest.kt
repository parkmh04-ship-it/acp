package com.acp.psp.application.service

import com.acp.psp.application.port.output.*
import com.acp.psp.generated.jooq.tables.pojos.Payments
import com.acp.schema.payment.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClientRequestException
import java.net.SocketTimeoutException

/** PaymentService 단위 테스트 */
class PaymentServiceTest {

    private val paymentRepositoryPort = mockk<PaymentRepositoryPort>(relaxed = true)
    private val paymentProvider = mockk<PaymentProvider>(relaxed = true)
    private val encryptionPort = mockk<EncryptionPort>(relaxed = true)
    private val paymentService = PaymentService(paymentRepositoryPort, paymentProvider, encryptionPort)

    @Test
    @DisplayName("기존 결제 기록이 없는 경우, 새 결제를 준비하고 저장해야 한다")
    fun `preparePayment signs new payment when no existing record found`() = runTest {
        // Given
        val request = PaymentPrepareRequest(
            merchantOrderId = "ORDER-123",
            amount = 10000L,
            items = listOf(PaymentItem("테스트 상품", 1, 10000L))
        )
        val mockPrepareResult = PaymentPrepareResult(
            pgTid = "TID-ABC-123",
            redirectUrl = "https://kakaopay.com/pay/abc123"
        )

        coEvery { paymentRepositoryPort.findLastByMerchantOrderIdAndType("ORDER-123", "PREPARE") } returns null
        coEvery { paymentProvider.prepare(any()) } returns mockPrepareResult
        coEvery { paymentProvider.providerName } returns "KAKAOPAY"
        coEvery { encryptionPort.encrypt("TID-ABC-123") } returns "ENCRYPTED-TID"

        // When
        val response = paymentService.preparePayment(request)

        // Then
        assertNotNull(response.paymentId)
        assertEquals("ORDER-123", response.merchantOrderId)
        assertEquals("READY", response.status)

        coVerify(exactly = 1) { paymentRepositoryPort.save(match { it.type == "PREPARE" && it.status == "READY" && it.pgTid == "ENCRYPTED-TID" }) }
    }

    @Test
    @DisplayName("결제 승인 성공 시 APPROVE 레코드가 저장되어야 한다")
    fun `approvePayment success`() = runTest {
        // Given
        val request = PaymentApproveRequest(merchantOrderId = "ORDER-123", pgToken = "TOKEN-123")
        val prepareRecord = Payments(
            id = "PAY-1", 
            merchantOrderId = "ORDER-123", 
            type = "PREPARE",
            status = "READY",
            amount = 10000L, 
            currency = "KRW",
            pgTid = "ENCRYPTED-TID"
        )
        
        coEvery { paymentRepositoryPort.findLastByMerchantOrderIdAndType("ORDER-123", "PREPARE") } returns prepareRecord
        coEvery { paymentRepositoryPort.findLastByMerchantOrderIdAndType("ORDER-123", "APPROVE") } returns null
        coEvery { encryptionPort.decrypt("ENCRYPTED-TID") } returns "TID-1"
        coEvery { encryptionPort.encrypt("TID-1") } returns "ENCRYPTED-TID"
        coEvery { paymentProvider.approve("TID-1", "ORDER-123", "TOKEN-123") } returns PaymentApproval(
            paymentId = "TID-1", pgTid = "TID-1", approvedAt = "2026-01-04T00:00:00", amount = 10000L, paymentMethod = "CARD"
        )

        // When
        val response = paymentService.approvePayment(request)

        // Then
        assertEquals("COMPLETED", response.status)
        coVerify(exactly = 1) { paymentRepositoryPort.save(match { it.type == "APPROVE" && it.status == "SUCCESS" }) }
    }

    @Test
    @DisplayName("타임아웃 발생 시 망취소(Net Cancel)를 실행해야 한다")
    fun `approvePayment network timeout triggers net cancel`() = runTest {
        // Given
        val request = PaymentApproveRequest(merchantOrderId = "ORDER-123", pgToken = "TOKEN-123")
        val prepareRecord = Payments(
            id = "PAY-1", 
            merchantOrderId = "ORDER-123", 
            type = "PREPARE",
            status = "READY",
            amount = 10000L, 
            currency = "KRW",
            pgTid = "ENCRYPTED-TID"
        )
        
        coEvery { paymentRepositoryPort.findLastByMerchantOrderIdAndType("ORDER-123", "PREPARE") } returns prepareRecord
        coEvery { paymentRepositoryPort.findLastByMerchantOrderIdAndType("ORDER-123", "APPROVE") } returns null
        coEvery { encryptionPort.decrypt("ENCRYPTED-TID") } returns "TID-1"
        
        // 승인 요청 중 타임아웃 발생 모킹 (메시지에 timeout 포함)
        coEvery { paymentProvider.approve(any(), any(), any()) } throws RuntimeException("Connection timeout")
        
        // 망취소 시나리오: 조회 시 PAID 상태면 취소 호출
        coEvery { paymentProvider.checkStatus("TID-1") } returns PaymentStatusInfo(status = "PAID", amount = 10000L)
        coEvery { paymentProvider.cancel(any(), any(), any()) } returns PaymentCancelResult("TID-1", "NOW", 10000L, "CANCELED")

        // When
        try {
            paymentService.approvePayment(request)
        } catch (e: Exception) {
            // Then
            coVerify(exactly = 1) { paymentProvider.checkStatus("TID-1") }
            coVerify(exactly = 1) { paymentProvider.cancel("TID-1", 10000L, any()) }
            coVerify(exactly = 1) { paymentRepositoryPort.save(match { it.type == "CANCEL" && it.status == "SUCCESS" }) }
        }
    }
}
