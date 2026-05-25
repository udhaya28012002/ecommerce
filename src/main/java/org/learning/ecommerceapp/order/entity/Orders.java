package org.learning.ecommerceapp.order.entity;

import jakarta.persistence.*;
import org.learning.ecommerceapp.user.entity.Users;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private long orderId;

    private String orderNumber;

    private LocalDateTime orderDate;

    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)
    private List<OrderItems> orderItemsList;

    private double finalPrice;

    public String getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(String appliedCoupon) {
        this.appliedCoupon = appliedCoupon;
    }

    private String appliedCoupon;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    public Orders(){}

    public Orders(LocalDateTime orderDate, OrderStatus orderStatus, List<OrderItems> orderItemsList, String orderNumber, double finalPrice) {
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderItemsList = orderItemsList;
        this.orderNumber = orderNumber;
        this.finalPrice = finalPrice;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    public long getOrderId() {
        return orderId;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
