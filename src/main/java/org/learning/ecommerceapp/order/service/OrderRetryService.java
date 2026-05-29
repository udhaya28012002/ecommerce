package org.learning.ecommerceapp.order.service;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleStateException;
import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.dto.response.OrdersResDto;
import org.learning.ecommerceapp.order.exception.OrderItemsNotFoundException;
import org.learning.ecommerceapp.order.exception.OrderProcessingException;
import org.learning.ecommerceapp.order.exception.ProductOutOfStockException;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class OrderRetryService {

    private final OrderService orderTransactionService;
private final CurrentUserService currentUserService;

    private final Logger logger = LoggerFactory.getLogger(OrderRetryService.class);

    public OrderRetryService(OrderService orderTransactionService, CurrentUserService currentUserService) {
        this.orderTransactionService = orderTransactionService;
        this.currentUserService = currentUserService;
    }

    public OrdersResDto placeOrderRetry(PlaceOrderRequest request) {

        String loggedUser = currentUserService.getLoggedInUser();

        int retries = 5;

        while (retries > 0) {

            try {

                return orderTransactionService.placeOrder(request);

            } catch (
                    ObjectOptimisticLockingFailureException |
                    OptimisticLockException |
                    StaleStateException ex
            ) {

                retries--;

                logger.warn(
                        "Optimistic locking failure for user {}. Retries left: {}",
                        loggedUser,
                        retries
                );

                if (retries == 0) {
                    throw new OrderProcessingException(
                            "Too many concurrent requests. Please try again"
                    );
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();

                    throw new OrderProcessingException(
                            "Order processing interrupted"
                    );
                }

            } catch (
                    ProductOutOfStockException |
                    OrderItemsNotFoundException ex
            ) {
                throw ex;
            }
        }

        throw new IllegalStateException("Unexpected execution path");
    }
}