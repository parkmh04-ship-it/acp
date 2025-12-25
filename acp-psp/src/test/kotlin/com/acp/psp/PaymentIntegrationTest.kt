package com.acp.psp

import com.acp.schema.payment.PaymentItem
import com.acp.schema.payment.PaymentPrepareRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentIntegrationTest(
    @Autowired val webTestClient: WebTestClient
) {

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
            .jsonPath("$.redirectUrl").isNotEmpty
    }
}
