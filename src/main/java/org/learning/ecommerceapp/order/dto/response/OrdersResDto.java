package org.learning.ecommerceapp.order.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.learning.ecommerceapp.order.entity.OrderStatus;

import java.util.List;

public class OrdersResDto {

    @NotBlank
    private String orderNumber;

    @NotNull
    private OrderStatus orderStatus;

    private List<OrderItemsResponseDto> orderItemsResponse;

    public List<OrderItemsResponseDto> getOrderItemsResponse() {
        return orderItemsResponse;
    }

    public void setOrderItemsResponse(List<OrderItemsResponseDto> orderItemsResponse) {
        this.orderItemsResponse = orderItemsResponse;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}