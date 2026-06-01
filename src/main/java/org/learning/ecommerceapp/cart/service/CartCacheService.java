package org.learning.ecommerceapp.cart.service;

import org.learning.ecommerceapp.cart.dto.response.CartCategoryResponseDto;
import org.learning.ecommerceapp.cart.dto.response.CartItemsResponseDto;
import org.learning.ecommerceapp.cart.dto.response.CartResponseDto;
import org.learning.ecommerceapp.cart.entity.Cart;
import org.learning.ecommerceapp.cart.entity.CartItems;
import org.learning.ecommerceapp.cart.exception.CartEmptyException;
import org.learning.ecommerceapp.cart.repository.CartItemRepository;
import org.learning.ecommerceapp.cart.repository.CartRepository;
import org.learning.ecommerceapp.order.exception.ProductOutOfStockException;
import org.learning.ecommerceapp.products.entity.Products;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.learning.ecommerceapp.products.service.ProductService;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.service.UserService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartCacheService {

    private final ProductService productService;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(CartCacheService.class);

    public CartCacheService(ProductService productService, CartRepository cartRepository, CartItemRepository cartItemRepository, UserService userService) {
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
    }

    @Transactional
    @CacheEvict(value = "cart", key = "#loggedUser")
    public void addToCart(long productId, int quantity, String loggedUser) {

        logger.debug("Add to cart request received. User: {}, ProductId: {}, Quantity: {}", loggedUser, productId, quantity);

        Products product = productService.getProductByIdInternal(productId);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null) {

            logger.debug("Cart not found for user: {}. Creating new cart.", loggedUser);

            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        CartItems existingCartItems = cartItemRepository.findByCartAndProducts(cart, product);

        /*int totalRequestedQuantity = quantity;

        if (existingCartItems != null) {
            totalRequestedQuantity = existingCartItems.getQuantity() + quantity;
        }*/

        if (existingCartItems != null) {

            int availableQuantity = existingCartItems.getProducts().getInventory().getProductQuantity();

            if ((existingCartItems.getQuantity() + quantity) > availableQuantity) {

                logger.warn("Insufficient stock while adding to cart. User: {}, ProductId: {}", loggedUser, productId);

                throw new InvalidInventoryException("Insufficient stock for the requested quantity");
            }
        }

        validateStockAvailability(product.getInventory().getProductQuantity(), quantity);

        if (existingCartItems == null) {

            logger.debug("Adding new product to cart. User: {}, ProductId: {}", loggedUser, productId);

            CartItems newCartItems = new CartItems();

            newCartItems.setCart(cart);
            newCartItems.setProducts(product);
            newCartItems.setQuantity(quantity);

            cartItemRepository.save(newCartItems);

        } else {

            logger.debug("Updating existing cart item quantity. User: {}, ProductId: {}", loggedUser, productId);

            existingCartItems.setQuantity(existingCartItems.getQuantity() + quantity);

            cartItemRepository.save(existingCartItems);
        }

        logger.info("Product added to cart successfully. User: {}, ProductId: {}", loggedUser, productId);

    }

    @Transactional
    @CacheEvict(value = "cart", key = "#loggedUser")
    public boolean removeFromCart(long productId, String loggedUser) {

        logger.debug("Remove from cart request received. User: {}, ProductId: {}", loggedUser, productId);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {

            logger.warn("Cart is empty for user: {}", loggedUser);

            throw new CartEmptyException("No products available in cart");
        }

        cart.getCartItemsList().removeIf(cartItems -> cartItems.getProducts().getProductId() == productId);

        if (!cartItemRepository.existsByCartAndProducts_ProductId(cart, productId)) {

            logger.warn("Product not found in cart. User: {}, ProductId: {}", loggedUser, productId);

            throw new CartEmptyException("No Product Found to Remove from Cart");
        }

        boolean removed = cartItemRepository.deleteByCartAndProducts_ProductId(cart, productId).getCartItemsId() == 0;

        logger.info("Product removed from cart successfully. User: {}, ProductId: {}", loggedUser, productId);

        return removed;
    }

    @Transactional
    @CacheEvict(value = "cart", key = "#loggedUser")
    public void updateCartQuantity(long productId, int quantity, boolean positive, String loggedUser) {

        logger.debug("Update cart quantity request received. User: {}, ProductId: {}, Quantity: {}, Positive: {}", loggedUser, productId, quantity, positive);

        if (quantity <= 0) {

            logger.warn("Invalid quantity provided. Quantity: {}", quantity);

            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {

            logger.warn("Cart is empty for user: {}", loggedUser);

            throw new CartEmptyException("No products available in cart");
        }

        CartItems cartItem = cart.getCartItemsList()
                .stream()
                .filter(item -> item.getProducts().getProductId() == productId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product not found in cart. User: {}, ProductId: {}", loggedUser, productId);
                    return new NoProductFound("Product not found in cart");
                });

        int currentQuantity = cartItem.getQuantity();

        int updatedQuantity = positive ? currentQuantity + quantity : currentQuantity - quantity;

        int availableQuantity = cartItem.getProducts().getInventory().getProductQuantity();

        if (updatedQuantity > availableQuantity) {

            logger.warn("Insufficient stock while updating cart. User: {}, ProductId: {}", loggedUser, productId);

            throw new InvalidInventoryException("Insufficient stock for the requested quantity");
        }

        if (updatedQuantity < 0) {

            logger.warn("Invalid updated quantity for ProductId: {}", productId);

            throw new InvalidInventoryException("Invalid quantity");
        }

        if (updatedQuantity == 0) {

            logger.debug("Quantity became zero. Removing product from cart. ProductId: {}", productId);

            cartItemRepository.delete(cartItem);
            cart.getCartItemsList().remove(cartItem);
        } else {

            logger.info("Cart quantity updated successfully. ProductId: {}, UpdatedQuantity: {}", productId, updatedQuantity);

            cartItem.setQuantity(updatedQuantity);
        }
    }

    @Transactional
    @CacheEvict(value = "cart", key = "#loggedUser")
    public void clearCart(String loggedUser) {

        logger.debug("Clear cart request received for user: {}", loggedUser);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        cart.getCartItemsList().clear();

        cartItemRepository.deleteByCart(cart);

        logger.info("Cart cleared successfully for user: {}", loggedUser);
    }

    @Cacheable(value = "cart", key = "#loggedUser")
    public CartResponseDto getUserCart(String loggedUser) {

        logger.debug("Fetching cart details for user: {}", loggedUser);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Cart cart = cartRepository.findByUsers(user);

        if (cart == null || cart.getCartItemsList().isEmpty()) {

            logger.warn("Cart is empty for user: {}", loggedUser);

            throw new CartEmptyException("No products available in cart");
        }

        logger.info("Cart details fetched successfully for user: {}", loggedUser);

        return buildResponseDto(cart.getCartItemsList());
    }

    private void validateStockAvailability(int stockQuantity, int requestedQuantity) {

        if (stockQuantity <= 0 || requestedQuantity > stockQuantity) {

            logger.warn("Product out of stock. Available: {}, Requested: {}", stockQuantity, requestedQuantity);

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
        cartResponseDto.setDiscount(0);
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
