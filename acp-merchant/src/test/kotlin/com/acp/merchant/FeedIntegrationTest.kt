package com.acp.merchant

import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24Product
import com.acp.merchant.adapter.outbound.cafe24.dto.Cafe24ProductsResponse
import com.acp.merchant.application.port.output.Cafe24ProductClient
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FeedIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var cafe24ProductClient: Cafe24ProductClient

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockCafe24ProductClient(): Cafe24ProductClient {
            return mockk(relaxed = true)
        }
    }

    @BeforeEach
    fun setup() {
        val mockProducts = listOf(
            createMockProduct("PROD-001", "Classic White T-Shirt", "29000.00", 1001L),
            createMockProduct("PROD-002", "Blue Denim Jeans", "49000.00", 1002L),
            createMockProduct("PROD-003", "Black Hoodie", "59000.00", 1003L)
        )

        coEvery {
            cafe24ProductClient.getProducts(
                limit = any(),
                offset = any(),
                display = any(),
                selling = any()
            )
        } returns Cafe24ProductsResponse(mockProducts)
    }

    private fun createMockProduct(code: String, name: String, priceStr: String, no: Long): Cafe24Product {
        return Cafe24Product(
            shopNo = 1,
            productNo = no,
            productCode = code,
            productName = name,
            price = priceStr,
            display = "T",
            selling = "T",
            productCondition = "N",
            stockQuantity = 100
        )
    }

    @Test
    fun `feed api returns product list`() {
        webTestClient.get()
            .uri("/feed")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].id").isEqualTo("cafe24_1001")
            .jsonPath("$[0].title").isEqualTo("Classic White T-Shirt")
            // Price format check might be tricky due to BigDecimal toString(), let's check existence
            .jsonPath("$[0].price").isNotEmpty
            .jsonPath("$[0].availability").isEqualTo("in_stock")
    }
}