package org.learning.ecommerceapp.discount.exception;

public class DiscountNotApplicable extends RuntimeException{
    public DiscountNotApplicable(String message) {
        super(message);
    }
}
