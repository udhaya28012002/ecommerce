package org.learning.ecommerceapp.user.exception;

public class PasswordReuseException extends RuntimeException {
    public PasswordReuseException(String s) {
        super(s);
    }
}
