package org.learning.ecommerceapp.order.repository;

import org.learning.ecommerceapp.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderServiceRepository extends JpaRepository<Orders, Long> {
}
