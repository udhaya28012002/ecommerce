package org.learning.ecommerceapp.cart.dto.request;

import jakarta.validation.constraints.Min;

public class AddToCartDto {

    private long productId;

    @Min(1)
    private int quantity;

    private String username;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
