package org.learning.ecommerceapp.order.controller;

import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.service.OrderRetryService;
import org.learning.ecommerceapp.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRetryService orderRetryService;

    public OrderController(OrderService orderService, OrderRetryService orderRetryService) {
        this.orderService = orderService;
        this.orderRetryService = orderRetryService;
    }

    @PostMapping("/placeOrder")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest placeOrderRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderRetryService.placeOrderRetry(placeOrderRequest));
    }

    @GetMapping("/getOrder/{orderNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/getOrders/username")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrdersByUsername(){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByUserName());
    }

    /*@GetMapping("/getOrders/emailId/{emailId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrdersByEmailId(@PathVariable String emailId){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByEmailId(emailId));
    }

    @GetMapping("/getOrders/contactNo/{contactNo}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrdersByContactNo(@PathVariable String contactNo){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByContactNo(contactNo));
    }*/

    @GetMapping("/getAllOrders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders(){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getAllOrders());
    }

    @PatchMapping("/updateOrder/pending/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToPending(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsPending(orderNumber));
    }

    @PatchMapping("/updateOrder/confirmed/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToConfirmed(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsConfirmed(orderNumber));
    }

    @PatchMapping("/updateOrder/processing/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToProcessing(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsProcessing(orderNumber));
    }

    @PatchMapping("/updateOrder/shipped/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToShipped(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markAsShipped(orderNumber));
    }

    @PatchMapping("/updateOrder/outForDelivery/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToOutForDelivery(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsOutForDelivery(orderNumber));
    }

    @PatchMapping("/updateOrder/delivered/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderToDelivered(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsDelivered(orderNumber));
    }

    @PatchMapping("/cancelOrder/{orderNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.cancelOrder(orderNumber));
    }
}
