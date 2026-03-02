package com.gogrow.product.api

import com.gogrow.product.domain.BillingCycle
import com.gogrow.product.domain.PriceType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateProductRequest(
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val isRentable: Boolean = false,
    val productImage: String? = null,
    val productDimension: String? = null,
    val prices: List<ProductPriceRequest> = emptyList(),
)

data class UpdateProductRequest(
    val name: String? = null,
    val category: String? = null,
    val description: String? = null,
    val isRentable: Boolean? = null,
    val productImage: String? = null,
    val productDimension: String? = null,
    val prices: List<ProductPriceRequest>? = null,
)

data class ProductPriceRequest(
    val priceType: PriceType,
    val amount: BigDecimal,
    val billingCycle: BillingCycle? = null,
    val securityDepositAmount: BigDecimal? = null,
    val isActive: Boolean = true,
)

data class ProductResponse(
    val id: UUID,
    val name: String,
    val category: String?,
    val description: String?,
    val isRentable: Boolean,
    val productImage: String?,
    val productDimension: String?,
    val createdAt: Instant,
    val modifiedAt: Instant?,
    val prices: List<ProductPriceResponse>,
)

data class ProductPriceResponse(
    val id: UUID,
    val productId: UUID,
    val priceType: PriceType,
    val amount: BigDecimal,
    val billingCycle: BillingCycle?,
    val securityDepositAmount: BigDecimal?,
    val isActive: Boolean,
    val createdAt: Instant,
    val modifiedAt: Instant?,
)
