package org.learning.ecommerceapp.cart.controller;

import org.apache.coyote.Response;
import org.learning.ecommerceapp.cart.dto.request.AddToCartDto;
import org.learning.ecommerceapp.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/getCart/{username}")
    public ResponseEntity<?> getCartDetails(@PathVariable String username) {
        return ResponseEntity.ok(cartService.getUserCart(username));
    }

    @PostMapping("/addToCart")
    public ResponseEntity<?> getCartDetails(@RequestBody AddToCartDto addToCartDto) {
        cartService.addToCart(addToCartDto.getProductId(), addToCartDto.getQuantity(), addToCartDto.getUsername());
        return ResponseEntity.ok("Added to Cart");
    }

    @DeleteMapping("/delete/{productId}/{username}")
    public ResponseEntity<?> removeEntryFromCart(@PathVariable long productId, @PathVariable String username) {
        return ResponseEntity.ok(cartService.removeFromCart(productId, username) ? "Deleted from Cart" : "Not Deleted from Cart");
    }

    @PatchMapping("/update")
    public ResponseEntity<?> UpdateCartQuantity(@RequestParam long productId, @RequestParam int quantity, @RequestParam boolean positive, @RequestParam String username) {
        cartService.updateCartQuantity(productId, quantity, positive, username);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/deleteAll/{username}")
    public ResponseEntity<?> removeAllItemsFromCart(@PathVariable String username) {
        cartService.clearCart(username);
        return ResponseEntity.ok().build();
    }
}
