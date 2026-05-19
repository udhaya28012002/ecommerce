package org.learning.ecommerceapp.category.repository;

import org.learning.ecommerceapp.category.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByCategoryNameIgnoreCase(String categoryName);

}
