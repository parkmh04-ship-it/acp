package com.acp.merchant.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Cafe24 API 설정
 *
 * Cafe24 API 호출을 위한 WebClient 및 관련 설정을 제공합니다.
 */
@Configuration
class Cafe24Config(
        @Value("\${cafe24.api.base-url}") private val baseUrl: String,
        @Value("\${cafe24.client-id}") private val clientId: String,
        @Value("\${cafe24.client-secret}") private val clientSecret: String,
        @Value("\${cafe24.access-token:}") private val accessToken: String
) {

    /**
     * Cafe24 API 호출용 WebClient
     *
     * - Base URL: Cafe24 API 엔드포인트
     * - Authorization: Bearer 토큰 인증
     * - Content-Type: application/json
     * - Timeout: 10초
     */
    @Bean
    fun cafe24WebClient(): WebClient {
        logger.info { "Initializing Cafe24 WebClient with baseUrl: $baseUrl" }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apply {
                    if (accessToken.isNotBlank()) {
                        logger.info { "Access token configured for Cafe24 API" }
                        defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                    } else {
                        logger.warn { "No access token configured. API calls may fail." }
                    }
                }
                .filter { request, next ->
                    logger.debug { "Cafe24 API Request: ${request.method()} ${request.url()}" }
                    next.exchange(request)
                            .doOnError { error ->
                                when (error) {
                                    is WebClientResponseException -> {
                                        logger.error {
                                            "Cafe24 API Error: ${error.statusCode} - ${error.responseBodyAsString}"
                                        }
                                    }
                                    else -> {
                                        logger.error(error) { "Cafe24 API Request failed" }
                                    }
                                }
                            }
                            .onErrorResume { error ->
                                logger.error(error) {
                                    "Cafe24 API call failed, returning empty response"
                                }
                                Mono.error(error)
                            }
                }
                .build()
    }

    /** Cafe24 OAuth 설정 정보 */
    @Bean
    fun cafe24OAuthConfig() =
            Cafe24OAuthConfig(clientId = clientId, clientSecret = clientSecret, baseUrl = baseUrl)
}

/** Cafe24 OAuth 설정 정보를 담는 데이터 클래스 */
data class Cafe24OAuthConfig(val clientId: String, val clientSecret: String, val baseUrl: String)
