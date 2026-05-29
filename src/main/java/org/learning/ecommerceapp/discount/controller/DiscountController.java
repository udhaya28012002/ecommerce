package org.learning.ecommerceapp.discount.controller;

import org.learning.ecommerceapp.discount.dto.AddDiscountDto;
import org.learning.ecommerceapp.discount.service.DiscountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/addCoupons/{filterPrice}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDiscountToUsers(@RequestBody AddDiscountDto addDiscountDto, @PathVariable double filterPrice){
        discountService.assignDiscountToEligibleUsers(addDiscountDto, filterPrice);
        return ResponseEntity.status(HttpStatus.CREATED).body("Coupons assigned successfully");
    }

    @PostMapping("/addCouponsToAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDiscountToAllUsers(@RequestBody AddDiscountDto addDiscountDto){
        discountService.assignDiscountToAllUsers(addDiscountDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Coupons assigned successfully");
    }

    @GetMapping("/displayCoupons")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> viewCoupons(){
        return ResponseEntity.ok(discountService.displayCoupons());
    }

    @GetMapping("/displayAllCoupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> viewAllCoupons(){
        return ResponseEntity.ok(discountService.displayAllCoupons());
    }

}
