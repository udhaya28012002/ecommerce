package org.learning.ecommerceapp.discount.exception;

public class NoCouponAvailable extends RuntimeException {
    public NoCouponAvailable(String message) {
        super(message);
    }
}
