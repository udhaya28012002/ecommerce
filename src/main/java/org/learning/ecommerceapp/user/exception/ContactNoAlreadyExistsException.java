package org.learning.ecommerceapp.user.exception;

public class ContactNoAlreadyExistsException extends ResourceAlreadyExistsException{
    public ContactNoAlreadyExistsException(String message) {
        super(message);
    }
}
