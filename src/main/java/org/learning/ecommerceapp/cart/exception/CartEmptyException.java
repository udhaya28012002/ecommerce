package org.learning.ecommerceapp.cart.exception;

public class CartEmptyException extends RuntimeException{
    public CartEmptyException(String message) {
        super(message);
    }
}
