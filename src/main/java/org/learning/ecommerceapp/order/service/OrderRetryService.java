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

    private final Logger log = LoggerFactory.getLogger(OrderRetryService.class);

    public OrderRetryService(OrderService orderTransactionService, CurrentUserService currentUserService) {
        this.orderTransactionService = orderTransactionService;
        this.currentUserService = currentUserService;
    }

    public OrdersResDto placeOrderRetry(PlaceOrderRequest request) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.info("Order retry process started for user: {}", loggedUser);

        int retries = 5;

        while (retries > 0) {

            try {

                log.info("Attempting order placement for user: {}. Remaining retries: {}", loggedUser, retries);

                return orderTransactionService.placeOrder(request);

            } catch (
                    ObjectOptimisticLockingFailureException |
                    OptimisticLockException |
                    StaleStateException ex
            ) {

                retries--;

                log.warn("Optimistic locking failure for user {}. Retries left: {}", loggedUser, retries
                );

                if (retries == 0) {

                    log.error("Order placement failed after maximum retries for user: {}", loggedUser);

                    throw new OrderProcessingException("Too many concurrent requests. Please try again");
                }

                try {

                    log.info("Waiting before retrying order placement for user: {}", loggedUser);

                    Thread.sleep(100);

                } catch (InterruptedException interruptedException) {

                    Thread.currentThread().interrupt();

                    log.error("Order processing interrupted for user: {}", loggedUser, interruptedException);

                    throw new OrderProcessingException("Order processing interrupted");
                }

            } catch (
                    ProductOutOfStockException |
                    OrderItemsNotFoundException ex
            ) {

                log.warn("Order placement failed for user: {}. Reason: {}", loggedUser, ex.getMessage());

                throw ex;
            }
        }

        throw new IllegalStateException("Unexpected execution path");
    }
}