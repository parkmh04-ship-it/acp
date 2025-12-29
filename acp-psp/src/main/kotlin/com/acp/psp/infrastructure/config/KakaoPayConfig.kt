package com.acp.psp.infrastructure.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = KotlinLogging.logger {}

/** 카카오페이 API 설정 */
@Configuration
class KakaoPayConfig(
        @Value("\${kakaopay.base-url}") private val baseUrl: String,
        @Value("\${kakaopay.secret-key}") private val secretKey: String
) {

    /**
     * 카카오페이 API 호출용 WebClient
     *
     * - 인증 방식: Secret Key (Dev/Production)
     * - Content-Type: application/json
     */
    @Bean
    fun kakaoPayWebClient(): WebClient {
        logger.info { "Initializing KakaoPay WebClient with baseUrl: $baseUrl" }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "SECRET_KEY $secretKey")
                .filter { request, next ->
                    logger.debug { "KakaoPay API Request: ${request.method()} ${request.url()}" }
                    next.exchange(request).doOnError { error ->
                        if (error is WebClientResponseException) {
                            logger.error {
                                "KakaoPay API Error: ${error.statusCode} - ${error.responseBodyAsString}"
                            }
                        } else {
                            logger.error(error) { "KakaoPay API Request failed" }
                        }
                    }
                }
                .build()
    }
}
