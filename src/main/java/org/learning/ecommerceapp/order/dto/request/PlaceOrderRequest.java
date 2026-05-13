package org.learning.ecommerceapp.order.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PlaceOrderRequest {

    @NotEmpty
    private List<OrderItemRequestDto> items;

    // optional fields
    // private Long addressId;
    // private String paymentMethod;

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
