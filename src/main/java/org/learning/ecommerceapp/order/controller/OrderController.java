package org.learning.ecommerceapp.order.controller;

import org.learning.ecommerceapp.order.dto.request.PlaceOrderRequest;
import org.learning.ecommerceapp.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest placeOrderRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(placeOrderRequest));
    }

    @GetMapping("/getOrder/{orderNumber}")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/getOrders/username/{userName}")
    public ResponseEntity<?> getOrdersByUsername(@PathVariable String userName){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByUserName(userName));
    }

    @GetMapping("/getOrders/emailId/{emailId}")
    public ResponseEntity<?> getOrdersByEmailId(@PathVariable String emailId){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByEmailId(emailId));
    }

    @GetMapping("/getOrders/contactNo/{contactNo}")
    public ResponseEntity<?> getOrdersByContactNo(@PathVariable String contactNo){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getUserOrdersByContactNo(contactNo));
    }

    @GetMapping("/getAllOrders/{userName}")
    public ResponseEntity<?> getAllOrders(@PathVariable String userName){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.getAllOrders(userName));
    }

    @PatchMapping("/updateOrder/pending/{orderNumber}")
    public ResponseEntity<?> updateOrderToPending(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsPending(orderNumber));
    }

    @PatchMapping("/updateOrder/confirmed/{orderNumber}")
    public ResponseEntity<?> updateOrderToConfirmed(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsConfirmed(orderNumber));
    }

    @PatchMapping("/updateOrder/processing/{orderNumber}")
    public ResponseEntity<?> updateOrderToProcessing(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsProcessing(orderNumber));
    }

    @PatchMapping("/updateOrder/shipped/{orderNumber}")
    public ResponseEntity<?> updateOrderToShipped(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markAsShipped(orderNumber));
    }

    @PatchMapping("/updateOrder/outForDelivery/{orderNumber}")
    public ResponseEntity<?> updateOrderToOutForDelivery(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsOutForDelivery(orderNumber));
    }

    @PatchMapping("/updateOrder/delivered/{orderNumber}")
    public ResponseEntity<?> updateOrderToDelivered(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.markOrderAsDelivered(orderNumber));
    }

    @PatchMapping("/cancelOrder/{orderNumber}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.cancelOrder(orderNumber));
    }
}
