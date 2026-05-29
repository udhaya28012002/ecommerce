package org.learning.ecommerceapp.user.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String addressCannotBeNull) {
        super(addressCannotBeNull);
    }
}
