package com.gogrow.product.api

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import com.gogrow.product.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
class ProductController(
    private val productService: ProductService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@RequestBody request: CreateProductRequest): ProductResponse =
        productService.createProduct(request)

    @PatchMapping("/{productId}")
    fun updateProduct(@PathVariable productId: UUID, @RequestBody request: UpdateProductRequest): ProductResponse =
        productService.updateProduct(productId, request)

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: UUID): ProductResponse =
        productService.getProduct(productId)

    @GetMapping
    fun listProducts(): List<ProductResponse> = productService.listProducts()
}
