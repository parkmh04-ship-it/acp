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
            .jsonPath("$.merchantOrderId").isEqualTo(request.merchantOrderId)
            .jsonPath("$.status").isEqualTo("READY")
            .jsonPath("$.paymentId").isNotEmpty
            .jsonPath("$.redirectUrl").isEqualTo("https://mock.kakaopay.com/redirect")
    }
}
