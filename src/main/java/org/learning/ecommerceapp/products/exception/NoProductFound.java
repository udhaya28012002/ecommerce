package org.learning.ecommerceapp.products.exception;

public class NoProductFound extends RuntimeException{
    public NoProductFound(String message) {
        super(message);
    }
}
