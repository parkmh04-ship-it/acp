package com.acp.merchant.adapter.outbound.web

import com.acp.merchant.application.port.output.PaymentClient
import com.acp.schema.payment.PaymentPrepareRequest
import com.acp.schema.payment.PaymentPrepareResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PaymentClientAdapter(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${psp.base-url}") private val pspUrl: String
) : PaymentClient {

    private val webClient: WebClient by lazy {
        webClientBuilder.baseUrl(pspUrl).build()
    }

    override suspend fun preparePayment(request: PaymentPrepareRequest): PaymentPrepareResponse {
        return webClient.post()
            .uri("/api/v1/payments/prepare")
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }
}
