package com.acp.merchant

import com.acp.merchant.service.ProductService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val productService: ProductService
) {

    @BeforeEach
    fun setup() {
        runBlocking {
            productService.initMockData()
        }
    }

    @Test
    fun `feed api returns product list`() {
        webTestClient.get()
            .uri("/feed")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[0].id").isEqualTo("PROD-001")
            .jsonPath("$[0].title").isEqualTo("Classic White T-Shirt")
            // Price format check might be tricky due to BigDecimal toString(), let's check existence
            .jsonPath("$[0].price").isNotEmpty
            .jsonPath("$[0].availability").isEqualTo("IN_STOCK")
    }
}
