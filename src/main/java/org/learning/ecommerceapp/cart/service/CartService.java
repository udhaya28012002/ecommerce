package org.learning.ecommerceapp.cart.service;

import org.learning.ecommerceapp.cart.dto.response.CartCategoryResponseDto;
import org.learning.ecommerceapp.cart.dto.response.CartItemsResponseDto;
import org.learning.ecommerceapp.cart.dto.response.CartResponseDto;
import org.learning.ecommerceapp.cart.entity.Cart;
import org.learning.ecommerceapp.cart.entity.CartItems;
import org.learning.ecommerceapp.cart.exception.CartEmptyException;
import org.learning.ecommerceapp.cart.repository.CartItemRepository;
import org.learning.ecommerceapp.cart.repository.CartRepository;
import org.learning.ecommerceapp.order.dto.request.OrderItemRequestDto;
import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.dto.response.OrderItemsResponseDto;
import org.learning.ecommerceapp.order.entity.OrderItems;
import org.learning.ecommerceapp.order.exception.ProductOutOfStockException;
import org.learning.ecommerceapp.order.service.OrderService;
import org.learning.ecommerceapp.products.entity.Products;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.learning.ecommerceapp.products.service.ProductService;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.service.UserService;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartService {

    private final ProductService productService;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final UserService userService;

    private final OrderService orderService;

    private final CurrentUserService currentUserService;

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    public CartService(ProductService productService, CartRepository cartRepository, CartItemRepository cartItemRepository, UserService userService, OrderService orderService, CurrentUserService currentUserService) {
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void addToCart(long productId, int quantity) {

        String loggedUser = currentUserService.getLoggedInUser();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Products product = productService.getProductByIdInternal(productId);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        CartItems existingCartItems = cartItemRepository.findByCartAndProducts(cart, product);

        int totalRequestedQuantity = quantity;

        if (existingCartItems != null) {
            totalRequestedQuantity = existingCartItems.getQuantity() + quantity;
        }

        validateStockAvailability(product.getInventory().getProductQuantity(), totalRequestedQuantity);

        if (existingCartItems == null) {

            CartItems newCartItems = new CartItems();

            newCartItems.setCart(cart);
            newCartItems.setProducts(product);
            newCartItems.setQuantity(quantity);

            cartItemRepository.save(newCartItems);

        } else {

            existingCartItems.setQuantity(existingCartItems.getQuantity() + quantity);

            cartItemRepository.save(existingCartItems);
        }
    }

    @Transactional
    public boolean removeFromCart(long productId) {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {
            throw new CartEmptyException("No products available in cart");
        }

        cart.getCartItemsList().removeIf(cartItems -> cartItems.getProducts().getProductId() == productId);

        if (!cartItemRepository.existsByCartAndProducts_ProductId(cart, productId)) {
            throw new CartEmptyException("No Product Found to Remove from Cart");
        }

        return cartItemRepository.deleteByCartAndProducts_ProductId(cart, productId);
    }

    @Transactional
    public void updateCartQuantity(long productId, int quantity, boolean positive) {

        String loggedUser = currentUserService.getLoggedInUser();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {
            throw new CartEmptyException("No products available in cart");
        }

        CartItems cartItem = cart.getCartItemsList()
                .stream()
                .filter(item -> item.getProducts().getProductId() == productId)
                .findFirst()
                .orElseThrow(() -> new NoProductFound("Product not found in cart"));

        int currentQuantity = cartItem.getQuantity();

        int updatedQuantity = positive ? currentQuantity + quantity : currentQuantity - quantity;

        int availableQuantity = cartItem.getProducts().getInventory().getProductQuantity();

        if (updatedQuantity > availableQuantity) {
            throw new InvalidInventoryException("Requested quantity exceeds available inventory");
        }

        if (updatedQuantity < 0) {
            throw new InvalidInventoryException("Invalid quantity");
        }

        if (updatedQuantity == 0) {
            cartItemRepository.delete(cartItem);
            cart.getCartItemsList().remove(cartItem);
        } else {
            cartItem.setQuantity(updatedQuantity);
        }
    }

    @Transactional
    public void clearCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        cart.getCartItemsList().clear();

        cartItemRepository.deleteByCart(cart);
    }

    public CartResponseDto getUserCart() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {
            throw new CartEmptyException("No products available in cart");
        }

        return buildResponseDto(cart.getCartItemsList());
    }


    private void validateStockAvailability(int stockQuantity, int requestedQuantity) {

        if (stockQuantity <= 0 || requestedQuantity > stockQuantity) {
            throw new ProductOutOfStockException("Product is out of stock");
        }
    }

    private CartResponseDto buildResponseDto(List<CartItems> cartItemsList) {


        Map<String, List<CartItemsResponseDto>> cartCategoryMap = new HashMap<>();

        for (CartItems cartItems : cartItemsList) {
            Products products = cartItems.getProducts();
            long categoryId = products.getProductCategory().getCategoryId();
            String categoryName = products.getProductCategory().getCategoryName();
            int quantity = cartItems.getQuantity();

            String computeKey = categoryName + ":" + categoryId;

            CartItemsResponseDto cartItemsResponseDto = buildCartItemsResponse(products, quantity);

            if (cartCategoryMap.containsKey(computeKey)) {
                cartCategoryMap.get(computeKey).add(cartItemsResponseDto);
            } else {
                List<CartItemsResponseDto> cartItemsResponseDtoList = new ArrayList<>();
                cartItemsResponseDtoList.add((cartItemsResponseDto));
                cartCategoryMap.put(computeKey, cartItemsResponseDtoList);
            }
        }

        return buildCartResponseDto(buildCartCategoryResponse(cartCategoryMap));
    }

    private CartResponseDto buildCartResponseDto(Set<CartCategoryResponseDto> cartCategoryResponseDtoList) {

        CartResponseDto cartResponseDto = new CartResponseDto();

        double computeTotalPrice = 0;

        for (CartCategoryResponseDto cartCategory : cartCategoryResponseDtoList) {
            for (CartItemsResponseDto cartItems : cartCategory.getCartItemsResponseDtoList()) {
                computeTotalPrice += (cartItems.getPrice() * cartItems.getQuantity());
            }
        }

        double computeFinalPrice = calculateOfferPrice(10, computeTotalPrice) + 100;

        cartResponseDto.setCartItemsCategoryResponseDtoList(cartCategoryResponseDtoList);
        cartResponseDto.setTotalPrice(computeTotalPrice);
        cartResponseDto.setDiscount(10);
        cartResponseDto.setDeliveryCharge(100);
        cartResponseDto.setFinalPrice(computeFinalPrice);

        return cartResponseDto;
    }

    private Set<CartCategoryResponseDto> buildCartCategoryResponse(Map<String, List<CartItemsResponseDto>> cartCategoryMap) {

        Set<CartCategoryResponseDto> cartCategoryResponseDto = new HashSet<>();

        cartCategoryMap.forEach(
                (key, cartItemsList) -> {
                    String[] keyValues = key.split(":");
                    String categoryName = keyValues[0];
                    long categoryId = Long.parseLong(keyValues[1]);
                    cartCategoryResponseDto.add(new CartCategoryResponseDto(categoryName, categoryId, cartItemsList));
                }
        );

        return cartCategoryResponseDto;
    }

    private CartItemsResponseDto buildCartItemsResponse(Products products, int quantity) {
        CartItemsResponseDto cartItemsResponseDto = new CartItemsResponseDto();

        cartItemsResponseDto.setProductId(products.getProductId());
        cartItemsResponseDto.setPrice(products.getPrice());
        cartItemsResponseDto.setProductName(products.getName());
        cartItemsResponseDto.setQuantity(quantity);
        cartItemsResponseDto.setAvailableStock(products.getInventory().getProductQuantity());
        cartItemsResponseDto.setSubtotal(quantity * products.getPrice());

        return cartItemsResponseDto;
    }

    private double calculateOfferPrice(int discount, double sellingPrice) {
        return sellingPrice * (1 - (discount / 100.0));
    }

}
