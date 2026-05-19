package org.learning.ecommerceapp.order.exception;

public class ProductOutOfStockException extends RuntimeException {

    public ProductOutOfStockException(String message) {
        super(message);
    }
}