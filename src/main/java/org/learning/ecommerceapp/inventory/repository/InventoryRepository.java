package org.learning.ecommerceapp.inventory.repository;

import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProducts_ProductId(long productId);

}
