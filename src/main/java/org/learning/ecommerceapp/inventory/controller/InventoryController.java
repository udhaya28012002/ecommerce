package org.learning.ecommerceapp.inventory.controller;

import org.learning.ecommerceapp.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PatchMapping("/updateInventory/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateInventoryQuantity(@PathVariable long productId, @RequestParam int quantity, @RequestParam boolean positive){
        return ResponseEntity.ok(inventoryService.updateInventoryQuantity(productId, quantity, positive));
    }
}
