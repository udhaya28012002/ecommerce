package org.learning.ecommerceapp.order.service;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleStateException;
import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.dto.response.OrdersResDto;
import org.learning.ecommerceapp.order.exception.OrderItemsNotFoundException;
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

    public OrdersResDto placeOrderRetry(PlaceOrderRequest request){

        String loggedUser = currentUserService.getLoggedInUser();

        int retries = 5;

        while(retries > 0){

            try {

                return orderTransactionService.placeOrder(request);

            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException | StaleStateException e){

                logger.warn("{} is trying {} time", loggedUser, retries);
                retries--;

                if(retries == 0){
                    throw new RuntimeException("Too many concurrent requests");
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (ProductOutOfStockException | OrderItemsNotFoundException ex){
                throw ex;
            }
        }
        throw new IllegalStateException("Unexpected execution path");
    }
}