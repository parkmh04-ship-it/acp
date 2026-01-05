package com.acp.psp

import com.acp.psp.application.port.output.PaymentPrepareResult
import com.acp.psp.application.port.output.PaymentProvider
import com.acp.schema.payment.PaymentItem
import com.acp.schema.payment.PaymentPrepareRequest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentIntegrationTest(
    @Autowired val webTestClient: WebTestClient
) {

    @Autowired
    lateinit var paymentProvider: PaymentProvider

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockPaymentProvider(): PaymentProvider = mockk()
    }

    @BeforeEach
    fun setup() {
        coEvery { paymentProvider.providerName } returns "KAKAOPAY"
        coEvery { paymentProvider.prepare(any()) } returns PaymentPrepareResult(
            pgTid = "T1234567890",
            redirectUrl = "https://mock.kakaopay.com/redirect"
        )
    }

    @Test
    fun `preparePayment returns successful response`() {
        val request = PaymentPrepareRequest(
            merchantOrderId = UUID.randomUUID().toString(),
            amount = 15000,
            items = listOf(
                PaymentItem("Test Product", 1, 15000)
            )
        )

        webTestClient.post()
            .uri("/api/v1/payments/prepare")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.merchant_order_id").isEqualTo(request.merchantOrderId)
            .jsonPath("$.status").isEqualTo("READY")
            .jsonPath("$.payment_id").isNotEmpty
            .jsonPath("$.redirect_url").isEqualTo("https://mock.kakaopay.com/redirect")
    }

    @Test
    fun `approvePayment returns successful response`() {
        val orderId = UUID.randomUUID().toString()
        
        // 1. Prepare first
        coEvery { paymentProvider.prepare(any()) } returns PaymentPrepareResult("TID-123", "http://redirect")
        webTestClient.post()
            .uri("/api/v1/payments/prepare")
            .bodyValue(PaymentPrepareRequest(orderId, 10000, items = emptyList()))
            .exchange()
            .expectStatus().isOk

        // 2. Approve
        coEvery { paymentProvider.approve("TID-123", orderId, "PG-TOKEN") } returns com.acp.psp.application.port.output.PaymentApproval(
            "TID-123", "TID-123", "2026-01-04T00:00:00", 10000, paymentMethod = "CARD"
        )

        val request = com.acp.schema.payment.PaymentApproveRequest(orderId, "PG-TOKEN")

        webTestClient.post()
            .uri("/api/v1/payments/approve")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("COMPLETED")
            .jsonPath("$.total_amount").isEqualTo(10000)
    }
}
