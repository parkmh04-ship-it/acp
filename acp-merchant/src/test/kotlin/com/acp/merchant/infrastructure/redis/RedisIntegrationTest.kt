package com.acp.merchant.infrastructure.redis

import com.acp.merchant.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate

@SpringBootTest(classes = [RedisAutoConfiguration::class])
class RedisIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @Test
    fun `Redis read and write verification`() {
        // Given
        val key = "test:key"
        val value = "Hello Redis"

        // When
        redisTemplate.opsForValue().set(key, value)
        val fetchedValue = redisTemplate.opsForValue().get(key)

        // Then
        assertEquals(value, fetchedValue)
    }

    @Test
    fun `Redis delete verification`() {
        // Given
        val key = "test:delete"
        redisTemplate.opsForValue().set(key, "to be deleted")

        // When
        redisTemplate.delete(key)
        val fetchedValue = redisTemplate.opsForValue().get(key)

        // Then
        assertNull(fetchedValue)
    }
}
