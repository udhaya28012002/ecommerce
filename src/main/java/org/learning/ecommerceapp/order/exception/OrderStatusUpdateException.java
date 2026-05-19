package org.learning.ecommerceapp.order.exception;

public class OrderStatusUpdateException extends RuntimeException {

    public OrderStatusUpdateException(String message) {
        super(message);
    }
}