package com.gogrow.product.service

import com.gogrow.product.api.CreateProductRequest
import com.gogrow.product.api.ProductPriceRequest
import com.gogrow.product.api.ProductPriceResponse
import com.gogrow.product.api.ProductResponse
import com.gogrow.product.api.UpdateProductRequest
import com.gogrow.product.domain.PriceType
import com.gogrow.product.domain.Product
import com.gogrow.product.domain.ProductPrice
import com.gogrow.product.repository.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        val productName = request.name.trim()
        if (productName.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required.")
        }

        val product = Product(
            name = productName,
            category = request.category?.trim()?.ifBlank { null },
            description = request.description?.trim()?.ifBlank { null },
            isRentable = request.isRentable,
            productImage = request.productImage?.trim()?.ifBlank { null },
            productDimension = request.productDimension?.trim()?.ifBlank { null },
        )

        product.prices.clear()
        product.prices.addAll(request.prices.map { toProductPrice(it, product) })

        return productRepository.save(product).toResponse()
    }

    @Transactional
    fun updateProduct(productId: UUID, request: UpdateProductRequest): ProductResponse {
        val product = productRepository.findWithPricesById(productId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.")

        request.name?.let {
            val normalizedName = it.trim()
            if (normalizedName.isBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name cannot be blank.")
            }
            product.name = normalizedName
        }
        request.category?.let { product.category = it.trim().ifBlank { null } }
        request.description?.let { product.description = it.trim().ifBlank { null } }
        request.isRentable?.let { product.isRentable = it }
        request.productImage?.let { product.productImage = it.trim().ifBlank { null } }
        request.productDimension?.let { product.productDimension = it.trim().ifBlank { null } }

        request.prices?.let { prices ->
            product.prices.clear()
            product.prices.addAll(prices.map { toProductPrice(it, product) })
        }

        return productRepository.save(product).toResponse()
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: UUID): ProductResponse {
        val product = productRepository.findWithPricesById(productId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.")
        return product.toResponse()
    }

    @Transactional(readOnly = true)
    fun listProducts(): List<ProductResponse> =
        productRepository.findAllByOrderByCreatedAtDesc().map { it.toResponse() }

    private fun toProductPrice(request: ProductPriceRequest, product: Product): ProductPrice {
        validateAmount(request.amount, "amount")
        request.securityDepositAmount?.let { validateAmount(it, "securityDepositAmount", allowZero = true) }

        if (request.priceType == PriceType.ONE_TIME && request.billingCycle != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "billingCycle must be null for ONE_TIME priceType.")
        }
        if (request.priceType == PriceType.RENTAL && request.billingCycle == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "billingCycle is required for RENTAL priceType.")
        }

        return ProductPrice(
            product = product,
            priceType = request.priceType,
            amount = request.amount,
            billingCycle = request.billingCycle,
            securityDepositAmount = request.securityDepositAmount,
            isActive = request.isActive,
        )
    }

    private fun validateAmount(amount: BigDecimal, fieldName: String, allowZero: Boolean = false) {
        if (allowZero) {
            if (amount < BigDecimal.ZERO) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName must be >= 0.")
            }
            return
        }
        if (amount <= BigDecimal.ZERO) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName must be > 0.")
        }
    }

    private fun Product.toResponse(): ProductResponse =
        ProductResponse(
            id = id!!,
            name = name,
            category = category,
            description = description,
            isRentable = isRentable,
            productImage = productImage,
            productDimension = productDimension,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            prices = prices.map { it.toResponse() },
        )

    private fun ProductPrice.toResponse(): ProductPriceResponse =
        ProductPriceResponse(
            id = id!!,
            productId = product.id!!,
            priceType = priceType,
            amount = amount,
            billingCycle = billingCycle,
            securityDepositAmount = securityDepositAmount,
            isActive = isActive,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
        )
}
