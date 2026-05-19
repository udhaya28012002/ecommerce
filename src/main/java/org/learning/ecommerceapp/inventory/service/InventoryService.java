package org.learning.ecommerceapp.inventory.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.inventory.repository.InventoryRepository;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public boolean updateInventoryQuantity(long productId, int quantity, boolean positive) {

        if (quantity <= 0) {
            throw new InvalidInventoryException("Quantity must be greater than 0");
        }

        Inventory inventory = inventoryRepository.findByProducts_ProductId(productId)
                .orElseThrow(() ->
                        new NoProductFound("No inventory found for productId: " + productId));

        int currentQty = inventory.getProductQuantity();
        int newQuantity;

        if (positive) {
            newQuantity = currentQty + quantity;
        } else {
            if (currentQty < quantity) {
                throw new InvalidInventoryException("Not enough stock to reduce");
            }
            newQuantity = currentQty - quantity;
        }

        inventory.setProductQuantity(newQuantity);

        return true;
    }
}
