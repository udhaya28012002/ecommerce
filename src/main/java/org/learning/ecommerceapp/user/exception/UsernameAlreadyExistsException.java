package org.learning.ecommerceapp.user.exception;

public class UsernameAlreadyExistsException extends ResourceAlreadyExistsException{
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
