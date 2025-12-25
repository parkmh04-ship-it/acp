package com.acp.merchant.controller

import com.acp.merchant.service.ProductService
import com.acp.schema.feed.ProductFeedItem
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FeedController(
    private val productService: ProductService
) {

    @GetMapping("/feed")
    suspend fun getFeed(): List<ProductFeedItem> {
        return productService.getProductFeed()
    }
}
