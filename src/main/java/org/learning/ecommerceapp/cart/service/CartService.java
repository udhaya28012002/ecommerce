package org.learning.ecommerceapp.cart.service;

import org.learning.ecommerceapp.cart.dto.response.CartResponseDto;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartCacheService cartCacheService;
    private final CurrentUserService currentUserService;

    public CartService(CartCacheService cartCacheService, CurrentUserService currentUserService) {
        this.cartCacheService = cartCacheService;
        this.currentUserService = currentUserService;
    }

    public void addToCart(long productId, int quantity) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Add to cart service invoked. User: {}, ProductId: {}, Quantity: {}", loggedUser, productId, quantity);

        if (quantity <= 0) {

            log.warn("Invalid quantity received for add to cart. Quantity: {}", quantity);

            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cartCacheService.addToCart(productId, quantity, loggedUser);

        log.info("Add to cart completed successfully. User: {}, ProductId: {}", loggedUser, productId);
    }

    public boolean removeFromCart(long productId) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Remove from cart service invoked. User: {}, ProductId: {}", loggedUser, productId);

        boolean removed = cartCacheService.removeFromCart(productId, loggedUser);

        log.info("Remove from cart completed. User: {}, ProductId: {}, Removed: {}", loggedUser, productId, removed);

        return removed;
    }

    public void updateCartQuantity(long productId, int quantity, boolean positive) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Update cart quantity service invoked. User: {}, ProductId: {}, Quantity: {}, Positive: {}", loggedUser, productId, quantity, positive);

        if (quantity <= 0) {

            log.warn("Invalid quantity received for cart update. Quantity: {}", quantity);

            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        cartCacheService.updateCartQuantity(productId, quantity, positive, loggedUser);

        log.info("Cart quantity updated successfully. User: {}, ProductId: {}", loggedUser, productId);
    }

    public void clearCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Clear cart service invoked for user: {}", loggedUser);

        cartCacheService.clearCart(loggedUser);

        log.info("Cart cleared successfully for user: {}", loggedUser);
    }

    public CartResponseDto getUserCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Get cart service invoked for user: {}", loggedUser);

        CartResponseDto cartResponseDto = cartCacheService.getUserCart(loggedUser);

        log.info("Cart fetched successfully for user: {}", loggedUser);

        return cartResponseDto;
    }
}