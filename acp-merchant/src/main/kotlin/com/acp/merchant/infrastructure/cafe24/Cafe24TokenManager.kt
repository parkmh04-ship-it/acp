package com.acp.merchant.infrastructure.cafe24

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class Cafe24TokenManager(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${cafe24.access-token:}") private val initialToken: String
) {
    private val TOKEN_KEY = "cafe24:access_token"

    /**
     * 유효한 액세스 토큰을 반환합니다.
     * Redis에 없으면 초기 설정된 토큰을 반환하거나 (임시), 향후 갱신 로직을 수행합니다.
     */
    fun getAccessToken(): String {
        val cachedToken = redisTemplate.opsForValue().get(TOKEN_KEY)
        if (cachedToken != null) {
            return cachedToken
        }
        
        // 초기 토큰이 있으면 Redis에 저장하고 반환 (Fail-safe)
        if (initialToken.isNotBlank()) {
            logger.info { "Using initial access token from config" }
            saveAccessToken(initialToken, Duration.ofHours(2)) // 보통 2시간 유효
            return initialToken
        }
        
        // TODO: Refresh Token을 이용한 자동 갱신 로직 구현 필요
        logger.warn { "No access token found in Redis or Config" }
        return ""
    }

    fun saveAccessToken(token: String, ttl: Duration) {
        redisTemplate.opsForValue().set(TOKEN_KEY, token, ttl)
    }
}
