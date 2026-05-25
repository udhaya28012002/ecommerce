package org.learning.ecommerceapp.user.exception;

public class UserAccessDeniedException extends RuntimeException{
    public UserAccessDeniedException(String message){
        super(message);
    }
}
