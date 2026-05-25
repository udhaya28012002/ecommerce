package org.learning.ecommerceapp.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.learning.ecommerceapp.cart.entity.Cart;
import org.learning.ecommerceapp.cart.entity.CartItems;
import org.learning.ecommerceapp.cart.exception.CartEmptyException;
import org.learning.ecommerceapp.discount.dto.ApplyCouponResponse;
import org.learning.ecommerceapp.discount.service.DiscountService;
import org.learning.ecommerceapp.order.dto.request.OrderItemRequestDto;
import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.dto.response.OrderItemsResponseDto;
import org.learning.ecommerceapp.order.dto.response.OrdersResDto;
import org.learning.ecommerceapp.order.entity.OrderItems;
import org.learning.ecommerceapp.order.entity.OrderStatus;
import org.learning.ecommerceapp.order.entity.Orders;
import org.learning.ecommerceapp.order.exception.OrderItemsNotFoundException;
import org.learning.ecommerceapp.order.exception.OrderNotFoundException;
import org.learning.ecommerceapp.order.exception.OrderStatusUpdateException;
import org.learning.ecommerceapp.order.exception.ProductOutOfStockException;
import org.learning.ecommerceapp.order.repository.OrderServiceRepository;
import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.learning.ecommerceapp.products.entity.Products;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.learning.ecommerceapp.products.service.ProductService;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.exception.UserAccessDeniedException;
import org.learning.ecommerceapp.user.repository.UserRepo;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {

    private final ProductService productService;
    private final OrderServiceRepository orderServiceRepository;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final DiscountService discountService;

    private static final int DEFAULT_DISCOUNT = 0;

    @PersistenceContext
    private EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(ProductService productService, OrderServiceRepository orderServiceRepository, UserRepo userRepo, CurrentUserService currentUserService, DiscountService discountService) {
        this.productService = productService;
        this.orderServiceRepository = orderServiceRepository;
        this.userRepo = userRepo;
        this.currentUserService = currentUserService;
        this.discountService = discountService;
    }

    public static final Map<OrderStatus, Set<OrderStatus>> VALID_STATUS_TRANSITIONS =
            Map.of(
                    OrderStatus.CREATED,
                    Set.of(OrderStatus.PENDING, OrderStatus.CANCELLED),

                    OrderStatus.PENDING,
                    Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),

                    OrderStatus.CONFIRMED,
                    Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),

                    OrderStatus.PROCESSING,
                    Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),

                    OrderStatus.SHIPPED,
                    Set.of(OrderStatus.OUT_OF_DELIVERY),

                    OrderStatus.OUT_OF_DELIVERY,
                    Set.of(OrderStatus.DELIVERED),

                    OrderStatus.DELIVERED,
                    Set.of(),

                    OrderStatus.CANCELLED,
                    Set.of()
            );

    @Transactional
    public OrdersResDto placeOrder(PlaceOrderRequest placeOrderRequest) throws OrderItemsNotFoundException, ProductOutOfStockException {


        int discount = 0;

        double deliveryCharge = 0;

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        if (placeOrderRequest.getItems() == null || placeOrderRequest.getItems().isEmpty()) {
            throw new OrderItemsNotFoundException("Order info is missing");
        }

        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        Orders order = new Orders();
        order.setOrderDate(today);
        order.setUsers(user);
        order.setOrderNumber("ORD-" + today.format(formatter));

        List<OrderItems> orderItemsList = buildOrderItems(placeOrderRequest, order, discount, deliveryCharge);

        double totalPricePerOrder = orderItemsList.stream()
                .mapToDouble(OrderItems::getTotalPrice)
                .sum();

        ApplyCouponResponse applyCouponResponse = checkCouponsAndRedeem(placeOrderRequest.getCouponCode(), totalPricePerOrder);

        double finalPrice = applyCouponResponse.getFinalPrice();
        order.setFinalPrice(finalPrice);

        order.setAppliedCoupon(applyCouponResponse.getCouponName());

        order.setOrderItemsList(orderItemsList);

        // PAYMENT PAGE (IF SUCCESS WE NEED TO PLACE ORDER OTHERWISE REVERT),

        updatingStocks(orderItemsList);

        order.setOrderStatus(OrderStatus.CONFIRMED);

        Orders savedOrder = orderServiceRepository.save(order);

        if(savedOrder.getOrderStatus().equals(OrderStatus.CONFIRMED)){
            logger.info("{}'s Order is confirmed : Order ID ({})",loggedUser, savedOrder.getOrderId());
        }

        return buildOrderResDto(savedOrder);
    }

    public void checkOut() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        Cart cart = user.getCart();

        if (cart == null || cart.getCartItemsList().isEmpty()) {
            throw new CartEmptyException("No products available in cart");
        }

        PlaceOrderRequest prepareOrderReq = new PlaceOrderRequest();
        prepareOrderReq.setItems(prepareOrderFromCart(cart.getCartItemsList()));
        placeOrder(prepareOrderReq);
    }

    private ApplyCouponResponse checkCouponsAndRedeem(String coupon, double totalPricePerOrder) {

        if (!discountService.validateCoupon(coupon)) {
            logger.info("Coupon Code Is Not Available");
            ApplyCouponResponse couponResponse = new ApplyCouponResponse();
            couponResponse.setApplied(false);
            couponResponse.setMessage("No Coupon is applied");
            couponResponse.setFinalPrice(totalPricePerOrder);
            return couponResponse;
        }

        ApplyCouponResponse applyCouponResponse = discountService.applyDiscountByUsers(coupon, totalPricePerOrder);

        if (!applyCouponResponse.isApplied()) {
            logger.info(applyCouponResponse.getMessage());
        }

        return applyCouponResponse;
    }

    private List<OrderItemRequestDto> prepareOrderFromCart(List<CartItems> cartItems) {

        List<OrderItemRequestDto> orderItemsList = new ArrayList<>();

        for (CartItems cartItem : cartItems) {
            OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto();
            orderItemRequestDto.setProductId(cartItem.getProducts().getProductId());
            orderItemRequestDto.setQuantity(cartItem.getQuantity());
            orderItemsList.add(orderItemRequestDto);
        }

        return orderItemsList;
    }


    public OrdersResDto getOrderByOrderNumber(String orderNumber) {

        String loggedUser = currentUserService.getLoggedInUser();

        Orders orders = orderServiceRepository.findByOrderNumber(orderNumber);

        if (orders == null) {
            throw new OrderNotFoundException("No Orders present with this OrderNumber : " + orderNumber);
        }

        if (!orders.getUsers().getUserName().equals(loggedUser)) {
            throw new OrderNotFoundException("No Order Found");
        }
        return buildOrderResDto(orders);
    }

    public List<OrdersResDto> getUserOrdersByUserName() {

        String loggedUser = currentUserService.getLoggedInUser();

        List<Orders> orders = orderServiceRepository.findByUsers_UserName(loggedUser);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found.");
        }

        return orders.stream()
                .map(this::buildOrderResDto)
                .toList();
    }

    /*public List<OrdersResDto> getUserOrdersByEmailId(String emailId) {

        String loggedUser = currentUserService.getLoggedInUser();

        if(!userRepo.findByUserName(loggedUser).getEmailId().equals(emailId)){
            throw new AccessDeniedException("Access Denied");
        }

        List<Orders> orders = orderServiceRepository.findByUsers_EmailId(emailId);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found.");
        }

        return orders.stream()
                .map(this::buildOrderResDto)
                .toList();
    }

    public List<OrdersResDto> getUserOrdersByContactNo(String contactNo) {
        List<Orders> orders = orderServiceRepository.findByUsers_ContactNo(contactNo);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found.");
        }

        return orders.stream()
                .map(this::buildOrderResDto)
                .toList();
    }*/

    public List<OrdersResDto> getAllOrders() {

        List<Orders> ordersList = orderServiceRepository.findAll();

        if (ordersList.isEmpty()) {
            throw new OrderNotFoundException("No orders found.");
        }

        return ordersList.stream()
                .map(this::buildOrderResDto)
                .toList();
    }

    @Transactional
    private void updateOrderStatus(String orderNumber, OrderStatus newOrderStatus) {
        Orders orders = getOrder(orderNumber);

        validateOrderStatusUpdate(orders.getOrderStatus(), newOrderStatus);

        orders.setOrderStatus(newOrderStatus);
        orderServiceRepository.save(orders);
    }

    public String markOrderAsPending(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.PENDING);
        return "Status Changed";
    }

    public String markOrderAsConfirmed(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.CONFIRMED);
        return "Status Changed";
    }

    public String markOrderAsProcessing(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.PROCESSING);
        return "Status Changed";
    }

    public String markAsShipped(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.SHIPPED);
        return "Status Changed";
    }

    public String markOrderAsOutForDelivery(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.OUT_OF_DELIVERY);
        return "Status Changed";
    }

    public String markOrderAsDelivered(String orderNumber) {
        updateOrderStatus(orderNumber, OrderStatus.DELIVERED);
        return "Status Changed";
    }

    @Transactional
    public String cancelOrder(String orderNumber) {

        String loggedUser = currentUserService.getLoggedInUser();

        Orders orders = getOrder(orderNumber);

        if (!orders.getUsers().getUserName().equals(loggedUser)) {
            throw new UserAccessDeniedException("Access Denied");
        }

        validateOrderStatusUpdate(orders.getOrderStatus(), OrderStatus.CANCELLED);

        //Rever the Inventory Count
        revertInventory(orders);

        //Updating the Order Status
        orders.setOrderStatus(OrderStatus.CANCELLED);

        orderServiceRepository.save(orders);

        return "Order Cancelled Successfully";
    }

    private void revertInventory(Orders orders) {
        List<OrderItems> orderItemsList = orders.getOrderItemsList();

        if (orderItemsList.isEmpty()) {
            throw new OrderItemsNotFoundException("No Order Items Found");
        }

        for (OrderItems orderItems : orderItemsList) {
            Products products = orderItems.getProduct();

            if (products == null) {
                throw new NoProductFound("No Product Found");
            }

            Inventory inventory = products.getInventory();
            inventory.setProductQuantity(inventory.getProductQuantity() + orderItems.getQuantity());
        }
    }

    private Orders getOrder(String orderNumber) {
        Orders orders = orderServiceRepository.findByOrderNumber(orderNumber);

        if (orders == null) {
            throw new OrderNotFoundException("No Order Found");
        }

        return orders;
    }

    private void validateOrderStatusUpdate(OrderStatus prevOrderStatus,
                                           OrderStatus newOrderStatus) {

        // Prevent same status update
        if (prevOrderStatus == newOrderStatus) {
            throw new OrderStatusUpdateException(
                    "Order is already in " + newOrderStatus + " status"
            );
        }

        Set<OrderStatus> allowedStatuses = VALID_STATUS_TRANSITIONS.get(prevOrderStatus);

        if (!allowedStatuses.contains(newOrderStatus)) {

            throw new OrderStatusUpdateException(
                    "Invalid status transition from "
                            + prevOrderStatus +
                            " to " +
                            newOrderStatus
            );
        }
    }

    private OrdersResDto buildOrderResDto(Orders savedOrder) {
        OrdersResDto ordersResDto = new OrdersResDto();
        ordersResDto.setOrderNumber(savedOrder.getOrderNumber());
        ordersResDto.setOrderStatus(savedOrder.getOrderStatus());

        ordersResDto.setOrderItemsResponse(
                savedOrder.getOrderItemsList().stream()
                        .map(item -> new OrderItemsResponseDto(
                                item.getQuantity(),
                                item.getSellingPrice(),
                                item.getDiscount(),
                                item.getTotalPrice()
                        ))
                        .toList()
        );
        ordersResDto.setFinalPrice(savedOrder.getFinalPrice());
        ordersResDto.setAppliedCoupon(savedOrder.getAppliedCoupon());
        return ordersResDto;
    }

    private void updatingStocks(List<OrderItems> orderItemsList) {

        for (OrderItems orderItems : orderItemsList) {

            Products products = orderItems.getProduct();
            int inventoryStock = products.getInventory().getProductQuantity();
            int orderQuantity = orderItems.getQuantity();
            Inventory inventory = products.getInventory();

            inventory.setProductQuantity(
                    calculateInventory(
                            inventory.getProductQuantity(),
                            orderQuantity
                    )
            );

            entityManager.flush();
        }

    }

    private List<OrderItems> buildOrderItems(PlaceOrderRequest placeOrderRequest, Orders order, int discount, double deliveryCharge) throws ProductOutOfStockException{
        List<OrderItems> orderItemsList = new ArrayList<>();
        for (OrderItemRequestDto dto : placeOrderRequest.getItems()) {

            Products product = productService.getProductByIdInternal(dto.getProductId());

            int inventoryStock = product.getInventory().getProductQuantity();

            validateIfProductIsAvailableForOrder(inventoryStock, dto.getQuantity());

            double discountForProduct = (discount > 0) ? dto.getQuantity() * calculateOfferPrice(discount, product.getPrice()) + deliveryCharge : dto.getQuantity() * product.getPrice() + deliveryCharge;

            OrderItems item = new OrderItems(
                    product,
                    order,
                    dto.getQuantity(),
                    product.getPrice(),
                    DEFAULT_DISCOUNT,
                    discountForProduct,
                    deliveryCharge
            );

            orderItemsList.add(item);
        }

        return orderItemsList;
    }

    private void validateIfProductIsAvailableForOrder(int stockQuantity, int buyQuantity) throws ProductOutOfStockException{
        if (stockQuantity <= 0 || buyQuantity > stockQuantity) {
            throw new ProductOutOfStockException("Product is out of stock");
        }
    }

    private int calculateInventory(int stockQuantity, int buyQuantity) {
        return stockQuantity - buyQuantity;
    }

    private double calculateOfferPrice(int discount, double sellingPrice) {
        return sellingPrice * (1 - (discount / 100.0));
    }


    /*private PlaceOrderRequest createOrderItems(List<OrderItemRequestDto> orderItemRequestDtoList) {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setItems(orderItemRequestDtoList);
        return placeOrderRequest;
    }*/
}
