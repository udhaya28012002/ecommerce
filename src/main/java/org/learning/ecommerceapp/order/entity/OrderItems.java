package org.learning.ecommerceapp.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.query.Order;
import org.hibernate.validator.constraints.Range;
import org.learning.ecommerceapp.products.entity.Products;

@Entity
@Table(name = "order_items")
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long orderItemId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products products;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private Orders orders;

    @Min(1)
    private int quantity;

    private double sellingPrice;

    @Range(min = 0, max = 70)
    private int discount;

    private double totalPrice;

    public OrderItems(){}
    public OrderItems(Products products, Orders orders, int quantity, double sellingPrice, int discount, double totalPrice) {
        this.products = products;
        this.orders = orders;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
    }

    public Products getProduct() {
        return products;
    }

    public void setProduct(Products products) {
        this.products = products;
    }

    public Orders getOrder() {
        return orders;
    }

    public void setOrder(Orders orders) {
        this.orders = orders;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getOrderItemId() {
        return orderItemId;
    }
}
