package com.acp.merchant

import com.acp.merchant.application.port.output.ProductPersistencePort
import com.acp.merchant.generated.jooq.tables.pojos.Products
import com.acp.merchant.generated.jooq.tables.references.*
import com.acp.schema.checkout.CreateCheckoutSessionRequest
import com.acp.schema.checkout.CheckoutItem
import com.acp.schema.checkout.Buyer
import com.acp.schema.checkout.Address
import com.acp.schema.checkout.UpdateCheckoutSessionRequest
import com.acp.merchant.application.port.output.Cafe24ProductClient
import com.acp.merchant.application.port.output.PaymentClient
import com.acp.schema.payment.PaymentPrepareResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CheckoutIntegrationTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductPersistencePort
    
    @Autowired
    lateinit var dsl: DSLContext
    
    @Autowired
    lateinit var paymentClient: PaymentClient

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockCafe24ProductClient(): Cafe24ProductClient {
            return mockk(relaxed = true)
        }
        
        @Bean
        @Primary
        fun mockPaymentClient(): PaymentClient {
            return mockk(relaxed = true)
        }
    }

    @BeforeEach
    fun setup() {
        runBlocking {
            withContext(Dispatchers.IO) {
                dsl.deleteFrom(CHECKOUT_ITEMS).execute()
                dsl.deleteFrom(CHECKOUT_SESSIONS).execute()
                dsl.deleteFrom(ORDER_LINES).execute()
                dsl.deleteFrom(ORDERS).execute()
                dsl.deleteFrom(PRODUCT_IMAGES).execute()
            }
            productRepository.deleteAll()
            productRepository.saveAll(listOf(
                Products(
                    id = "prod_1",
                    title = "Test Product 1",
                    description = "Desc 1",
                    link = "http://example.com/1",
                    imageLink = "http://example.com/img1.jpg",
                    priceAmount = BigDecimal("10000.0000"),
                    currency = "KRW",
                    availability = "in_stock",
                    inventoryQty = 100,
                    condition = "new",
                    createdAt = java.time.OffsetDateTime.now(),
                    updatedAt = java.time.OffsetDateTime.now()
                ),
                Products(
                    id = "prod_2",
                    title = "Test Product 2",
                    description = "Desc 2",
                    link = "http://example.com/2",
                    imageLink = "http://example.com/img2.jpg",
                    priceAmount = BigDecimal("20000.0000"),
                    currency = "KRW",
                    availability = "in_stock",
                    inventoryQty = 50,
                    condition = "new",
                    createdAt = java.time.OffsetDateTime.now(),
                    updatedAt = java.time.OffsetDateTime.now()
                )
            ))
        }
    }

    @Test
    fun `create checkout session successfully`() {
        val request = CreateCheckoutSessionRequest(
            items = listOf(
                CheckoutItem(id = "prod_1", quantity = 2), // 20000
                CheckoutItem(id = "prod_2", quantity = 1)  // 20000
            ),
            buyer = Buyer("test@example.com", "Tester"),
            fulfillmentAddress = Address("KR", "12345")
        )

        webTestClient.post()
            .uri("/checkout_sessions")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.status").isEqualTo("not_ready_for_payment")
            .jsonPath("$.currency").isEqualTo("KRW")
            .jsonPath("$.totals[?(@.type == 'total')].amount").isEqualTo(44000)
            .jsonPath("$.totals[?(@.type == 'tax')].amount").isEqualTo(4000)
    }

    @Test
    fun `complete checkout session calls psp`() {
        // ... (이전 코드 동일)
    }

    @Test
    fun `update checkout session items recalculates totals`() {
        // 1. Create Session with 1 product
        val request = CreateCheckoutSessionRequest(
            items = listOf(CheckoutItem(id = "prod_1", quantity = 1)),
            buyer = Buyer("test@example.com", "Tester"),
            fulfillmentAddress = Address("KR", "12345")
        )

        val createResult = webTestClient.post()
            .uri("/checkout_sessions")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .returnResult(com.acp.schema.checkout.CheckoutSessionResponse::class.java)
            
        val sessionId = createResult.responseBody.blockFirst()!!.id

        // 2. Update quantity to 5
        val updateRequest = UpdateCheckoutSessionRequest(
            items = listOf(CheckoutItem(id = "prod_1", quantity = 5))
        )

        webTestClient.post()
            .uri("/checkout_sessions/${sessionId}")
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.line_items[0].item.quantity").isEqualTo(5)
            // 10000 * 5 = 50000, Tax 10% = 5000, Total = 55000
            .jsonPath("$.totals[?(@.type == 'total')].amount").isEqualTo(55000)
            .jsonPath("$.totals[?(@.type == 'tax')].amount").isEqualTo(5000)
    }
}
