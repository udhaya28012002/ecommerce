package org.learning.ecommerceapp.cart.controller;

import org.apache.coyote.Response;
import org.learning.ecommerceapp.cart.dto.request.AddToCartDto;
import org.learning.ecommerceapp.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/getCart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCartDetails() {
        return ResponseEntity.ok(cartService.getUserCart());
    }

    @PostMapping("/addToCart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCartDetails(@RequestBody AddToCartDto addToCartDto) {
        cartService.addToCart(addToCartDto.getProductId(), addToCartDto.getQuantity());
        return ResponseEntity.ok("Added to Cart");
    }

    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> removeEntryFromCart(@PathVariable long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId) ? "Deleted from Cart" : "Not Deleted from Cart");
    }

    @PatchMapping("/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> UpdateCartQuantity(@RequestParam long productId, @RequestParam int quantity, @RequestParam boolean positive) {
        cartService.updateCartQuantity(productId, quantity, positive);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/deleteAll")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> removeAllItemsFromCart() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }
}
