package org.learning.ecommerceapp.cart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.learning.ecommerceapp.products.entity.Products;

@Entity
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartitems_id")
    private long cartItemsId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    private Products products;

    @Min(1)
    private int quantity;

    public long getCartItemsId() {
        return cartItemsId;
    }

    public void setCartItemsId(long cartItemsId) {
        this.cartItemsId = cartItemsId;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
