package org.learning.ecommerceapp.order.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PlaceOrderRequest {

    private String couponCode;

    @NotEmpty
    private List<OrderItemRequestDto> items;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public List<OrderItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDto> items) {
        this.items = items;
    }
}
