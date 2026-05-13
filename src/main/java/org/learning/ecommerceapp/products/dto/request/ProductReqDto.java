package org.learning.ecommerceapp.products.dto.request;

import jakarta.validation.constraints.*;

public class ProductReqDto {
    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @Positive
    private double price;

    @NotBlank
    @Size(min = 5, max = 200)
    private String shortDescription;

    @NotNull
    private Long categoryId;

    @PositiveOrZero
    private int quantity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}