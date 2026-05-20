package org.learning.ecommerceapp.cart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.learning.ecommerceapp.cart.repository.CartItemRepository;
import org.learning.ecommerceapp.cart.repository.CartRepository;
import org.learning.ecommerceapp.products.dto.request.ProductReqDto;
import org.learning.ecommerceapp.products.service.ProductService;
import org.learning.ecommerceapp.user.service.UserService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    ProductService productService;

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    UserService userService;

    @InjectMocks
    CartService cartService;

    @Test
    void addToCart() {
        ProductReqDto productReqDto = new ProductReqDto();
        String result = productService.addProduct(productReqDto);
        System.out.println(result);
    }

    @Test
    void removeFromCart() {
    }

    @Test
    void updateCartQuantity() {
    }

    @Test
    void clearCart() {
    }

    @Test
    void getUserCart() {
    }
}