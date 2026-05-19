package org.learning.ecommerceapp.inventory.exception;

public class InvalidInventoryException extends RuntimeException{
    public InvalidInventoryException(String message) {
        super(message);
    }
}
