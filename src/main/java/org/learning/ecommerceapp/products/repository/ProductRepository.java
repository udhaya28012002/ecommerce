package org.learning.ecommerceapp.products.repository;

import org.learning.ecommerceapp.products.dto.ProductRawDto;
import org.learning.ecommerceapp.products.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {

    List<Products> findByProductCategoryCategoryId(long categoryId);

    List<Products> findByPriceBetween(double minPrice, double maxPrice);

    // AVAILABLE PRODUCTS
    @Query("""
        SELECT new org.learning.ecommerceapp.products.dto.ProductRawDto(
            p.id,
            p.name,
            p.price,
            p.shortDescription,
            p.productCategory,
            p.inventory.productQuantity
        )
        FROM Products p
        WHERE p.inventory.productQuantity > 0
    """)
    List<ProductRawDto> findAvailableProducts();

    // SEARCH BY NAME
    @Query("""
        SELECT new org.learning.ecommerceapp.products.dto.ProductRawDto(
            p.id,
            p.name,
            p.price,
            p.shortDescription,
            p.productCategory,
            p.inventory.productQuantity
        )
        FROM Products p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<ProductRawDto> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
}