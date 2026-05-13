package org.learning.ecommerceapp.order.service;

import org.learning.ecommerceapp.order.dto.request.OrderItemRequestDto;
import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.dto.response.OrderItemsResponseDto;
import org.learning.ecommerceapp.order.dto.response.OrderPlacedResDto;
import org.learning.ecommerceapp.order.entity.OrderItems;
import org.learning.ecommerceapp.order.entity.OrderStatus;
import org.learning.ecommerceapp.order.entity.Orders;
import org.learning.ecommerceapp.order.repository.OrderServiceRepository;
import org.learning.ecommerceapp.products.entity.Products;
import org.learning.ecommerceapp.products.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductService productService;
    private final OrderServiceRepository orderServiceRepository;

    public OrderService(ProductService productService, OrderServiceRepository orderServiceRepository) {
        this.productService = productService;
        this.orderServiceRepository = orderServiceRepository;
    }

    @Transactional
    public OrderPlacedResDto placeOrder(PlaceOrderRequest placeOrderRequest) {

        long uniqueId = 0;
        LocalDateTime today = LocalDateTime.now();

        // 1. CREATE ORDER FIRST
        Orders order = new Orders();
        order.setOrderDate(today);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setOrderNumber("ORD-" + today + "-" + System.currentTimeMillis());

        List<OrderItems> orderItemsList = new ArrayList<>();

        // 2. CREATE ORDER ITEMS
        for (OrderItemRequestDto dto : placeOrderRequest.getItems()) {

            Products product = productService.getProductByIdInternal(dto.getProductId());

            OrderItems item = new OrderItems(
                    product,
                    order,
                    dto.getQuantity(),
                    product.getPrice(),
                    10,
                    dto.getQuantity() * calculateOfferPrice(10, product.getPrice())
            );

            uniqueId += product.getProductId();
            orderItemsList.add(item);
        }

        // 3. LINK ITEMS TO ORDER
        order.setOrderItemsList(orderItemsList);

        // 4. SAVE ONLY ORDER (cascade handles items)
        Orders savedOrder = orderServiceRepository.save(order);

        // 5. BUILD RESPONSE
        OrderPlacedResDto orderPlacedResDto = new OrderPlacedResDto();
        orderPlacedResDto.setOrderNumber(savedOrder.getOrderNumber());
        orderPlacedResDto.setOrderStatus(savedOrder.getOrderStatus());

        orderPlacedResDto.setOrderItemsResponse(
                savedOrder.getOrderItemsList().stream()
                        .map(item -> new OrderItemsResponseDto(
                                item.getQuantity(),
                                item.getSellingPrice(),
                                item.getDiscount(),
                                item.getTotalPrice()
                        ))
                        .toList()
        );

        return orderPlacedResDto;
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
