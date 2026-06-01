package org.learning.ecommerceapp.discount.repository;

import org.learning.ecommerceapp.discount.entity.GlobalDiscountOnProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalDiscountRepo extends JpaRepository<GlobalDiscountOnProducts, Long> {
}
