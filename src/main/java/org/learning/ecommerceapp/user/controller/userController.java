package org.learning.ecommerceapp.user.controller;

import jakarta.validation.Valid;
import org.learning.ecommerceapp.user.dto.request.*;
import org.learning.ecommerceapp.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class userController {

    private static final Logger log = LoggerFactory.getLogger(userController.class);

    private final UserService userService;

    @Autowired
    public userController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/getUser")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getUser() {
        log.info("Fetching logged-in user details");
        return ResponseEntity.ok(userService.getUserDetails());
    }

    @PatchMapping("/password")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.debug("Password change request received");
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @PatchMapping("/contactNo")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> changeContactNo(@Valid @RequestBody ChangeOtherDetailsReq request) {
        log.debug("Contact number update request received");
        return ResponseEntity.ok(userService.updateContactNo(request));
    }

    @PatchMapping("/address")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddAddressReq request) {
        log.debug("Address update request received");
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(request));
    }

    @PatchMapping("/deleteUser")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> deleteUser() {
        log.debug("Delete user request received");
        return ResponseEntity.ok(userService.deleteUser());
    }
}