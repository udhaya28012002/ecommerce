package org.learning.ecommerceapp.order.dto.response;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.learning.ecommerceapp.order.entity.OrderItems;
import org.learning.ecommerceapp.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderPlacedResDto {

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