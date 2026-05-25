package org.learning.ecommerceapp.cart.service;

import org.learning.ecommerceapp.cart.dto.response.CartResponseDto;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {

    private final CartCacheService cartCacheService;
    private final CurrentUserService currentUserService;

    public CartService(CartCacheService cartCacheService, CurrentUserService currentUserService) {
        this.cartCacheService = cartCacheService;
        this.currentUserService = currentUserService;
    }

    public void addToCart(long productId, int quantity) {

        String loggedUser = currentUserService.getLoggedInUser();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cartCacheService.addToCart(productId, quantity, loggedUser);
    }

    public boolean removeFromCart(long productId) {

        String loggedUser = currentUserService.getLoggedInUser();

        return cartCacheService.removeFromCart(productId, loggedUser);
    }

    public void updateCartQuantity(long productId, int quantity, boolean positive) {

        String loggedUser = currentUserService.getLoggedInUser();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cartCacheService.updateCartQuantity(productId, quantity, positive, loggedUser);
    }

    public void clearCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        cartCacheService.clearCart(loggedUser);
    }

    public CartResponseDto getUserCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        return cartCacheService.getUserCart(loggedUser);
    }

}
