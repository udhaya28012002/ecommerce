package org.learning.ecommerceapp.discount.controller;

import jakarta.validation.Valid;
import org.learning.ecommerceapp.discount.dto.AddDiscountDto;
import org.learning.ecommerceapp.discount.service.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
public class DiscountController {

    private static final Logger log = LoggerFactory.getLogger(DiscountController.class);

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/addCoupons/{filterPrice}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDiscountToUsers(@RequestBody AddDiscountDto addDiscountDto, @PathVariable double filterPrice){

        log.debug("Assign coupon to eligible users request received. FilterPrice: {}, CouponCode: {}", filterPrice, addDiscountDto.getCouponCode());

        discountService.assignDiscountToEligibleUsers(addDiscountDto, filterPrice);

        log.info("Coupons assigned successfully to eligible users. CouponCode: {}", addDiscountDto.getCouponCode());

        return ResponseEntity.status(HttpStatus.CREATED).body("Coupons assigned successfully");
    }

    @PostMapping("/addCouponsToAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDiscountToAllUsers(@Valid @RequestBody AddDiscountDto addDiscountDto){

        log.debug("Assign coupon to all users request received. CouponCode: {}", addDiscountDto.getCouponCode());

        discountService.assignDiscountToAllUsers(addDiscountDto);

        log.info("Coupons assigned successfully to all users. CouponCode: {}", addDiscountDto.getCouponCode());

        return ResponseEntity.status(HttpStatus.CREATED).body("Coupons assigned successfully");
    }

    @GetMapping("/displayCoupons")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> viewCoupons(){

        log.debug("Display coupons request received for customer");

        return ResponseEntity.ok(discountService.displayCoupons());
    }

    @GetMapping("/displayAllCoupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> viewAllCoupons(){

        log.debug("Display all coupons request received");

        return ResponseEntity.ok(discountService.displayAllCoupons());
    }

}
