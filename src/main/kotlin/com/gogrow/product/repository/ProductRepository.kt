package com.gogrow.product.repository

import com.gogrow.product.domain.Product
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = ["prices"])
    fun findWithPricesById(id: UUID): Product?

    @EntityGraph(attributePaths = ["prices"])
    fun findAllByOrderByCreatedAtDesc(): List<Product>
}
