package org.learning.ecommerceapp.inventory.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.inventory.controller.InventoryController;
import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.inventory.repository.InventoryRepository;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public boolean updateInventoryQuantity(long productId, int quantity, boolean positive) {

        log.debug("Inventory update service invoked. ProductId: {}, Quantity: {}, Positive: {}", productId, quantity, positive);

        if (quantity <= 0) {

            log.warn("Invalid inventory quantity provided. Quantity: {}", quantity);

            throw new InvalidInventoryException("Quantity must be greater than 0");
        }

        Inventory inventory = inventoryRepository.findByProducts_ProductId(productId)
                .orElseThrow(() -> {
                    log.warn(
                            "No inventory found for ProductId: {}", productId);
                    return new NoProductFound("No inventory found for productId: " + productId);
                });

        int currentQty = inventory.getProductQuantity();
        int newQuantity;

        if (positive) {


            newQuantity = currentQty + quantity;

            log.debug("Increasing inventory quantity. ProductId: {}, CurrentQty: {}, AddedQty: {}, NewQty: {}", productId, currentQty, quantity, newQuantity);

        } else {
            if (currentQty < quantity) {

                log.warn("Insufficient inventory stock reduction attempt. ProductId: {}, CurrentQty: {}, RequestedReduction: {}", productId, currentQty, quantity);

                throw new InvalidInventoryException("Not enough stock to reduce");
            }
            newQuantity = currentQty - quantity;

            log.debug("Reducing inventory quantity. ProductId: {}, CurrentQty: {}, ReducedQty: {}, NewQty: {}", productId, currentQty, quantity, newQuantity);
        }

        inventory.setProductQuantity(newQuantity);

        log.info("Inventory updated successfully. ProductId: {}, FinalQuantity: {}", productId, newQuantity);

        return true;
    }
}
