package com.gogrow.product.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "product_prices", schema = "core")
class ProductPrice(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 16)
    var priceType: PriceType,

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", length = 16)
    var billingCycle: BillingCycle? = null,

    @Column(name = "security_deposit_amount", precision = 12, scale = 2)
    var securityDepositAmount: BigDecimal? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "modified_at")
    var modifiedAt: Instant? = null,
) {
    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        modifiedAt = Instant.now()
    }
}
