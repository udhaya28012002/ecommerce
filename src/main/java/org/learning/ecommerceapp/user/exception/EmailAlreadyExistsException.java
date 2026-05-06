package org.learning.ecommerceapp.user.exception;

public class EmailAlreadyExistsException extends ResourceAlreadyExistsException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
