package org.learning.ecommerceapp.order.exception;

    public class OrderItemsNotFoundException extends RuntimeException{
        public OrderItemsNotFoundException(String message) {
            super(message);
        }
    }
