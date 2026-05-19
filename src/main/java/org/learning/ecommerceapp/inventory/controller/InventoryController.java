package org.learning.ecommerceapp.inventory.controller;

import org.learning.ecommerceapp.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PatchMapping("/updateInventory/{productId}")
    public ResponseEntity<?> updateInventoryQuantity(@PathVariable long productId, @RequestParam int quantity, @RequestParam boolean positive){
        return ResponseEntity.ok(inventoryService.updateInventoryQuantity(productId, quantity, positive));
    }
}
