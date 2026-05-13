package org.learning.ecommerceapp.products.exception;

public class InvalidInventoryException extends RuntimeException{
    public InvalidInventoryException(String message) {
        super(message);
    }
}
