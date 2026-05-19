package org.learning.ecommerceapp.cart.dto.response;

import java.util.List;

public class CartCategoryResponseDto {

    private String categoryName;

    private long categoryId;

    private List<CartItemsResponseDto> cartItemsResponseDtoList;

    public CartCategoryResponseDto(String categoryName, long categoryId, List<CartItemsResponseDto> cartItemsResponseDtoList) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.cartItemsResponseDtoList = cartItemsResponseDtoList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<CartItemsResponseDto> getCartItemsResponseDtoList() {
        return cartItemsResponseDtoList;
    }

    public void setCartItemsResponseDtoList(List<CartItemsResponseDto> cartItemsResponseDtoList) {
        this.cartItemsResponseDtoList = cartItemsResponseDtoList;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}
