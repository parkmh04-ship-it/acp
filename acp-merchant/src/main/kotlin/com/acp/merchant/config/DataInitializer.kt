package com.acp.merchant.config

import com.acp.merchant.application.port.input.ProductAdminUseCase
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataInitializer(private val productAdminUseCase: ProductAdminUseCase) {

    @Bean
    @Profile("!test") // Don't run in tests to avoid interference
    fun initData() = CommandLineRunner {
        runBlocking {
            productAdminUseCase.initMockData()
            println("✅ Mock Product Data Initialized")
        }
    }
}
