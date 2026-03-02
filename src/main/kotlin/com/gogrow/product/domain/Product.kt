package com.gogrow.product.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "products", schema = "core")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "category", length = 255)
    var category: String? = null,

    @Column(name = "description", columnDefinition = "text")
    var description: String? = null,

    @Column(name = "is_rentable", nullable = false)
    var isRentable: Boolean = false,

    @Column(name = "product_image", columnDefinition = "text")
    var productImage: String? = null,

    @Column(name = "product_dimension", columnDefinition = "text")
    var productDimension: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "modified_at")
    var modifiedAt: Instant? = null,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var prices: MutableList<ProductPrice> = mutableListOf(),
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
