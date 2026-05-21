package org.learning.ecommerceapp.user.controller;

import jakarta.validation.Valid;
import org.learning.ecommerceapp.user.dto.request.*;
import org.learning.ecommerceapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class userController {

    private final UserService userService;

    @Autowired
    public userController(UserService userService) {
        this.userService = userService;
    }

    /*@PostMapping("/login")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReqDto loginReq) {
        return ResponseEntity.ok(userService.login(loginReq));
    }

    @PostMapping("/createUser")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserReqDto userReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userReqDto, false));
    }*/

    @PostMapping("/getUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(@Valid @RequestBody LoginReqDto loginReq) {
        return ResponseEntity.ok(userService.getAllUsers(loginReq));
    }

    /*@PostMapping("/getUser")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getUser(@Valid @RequestBody LoginReqDto loginReq) {

        return ResponseEntity.ok(userService.getUser(loginReq));
    }*/

    @GetMapping("/getUser")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getUser() {
        return ResponseEntity.ok(userService.getUserDetails());
    }

    @PatchMapping("/password")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(request));
    }

    @PatchMapping("/contactNo")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> changeContactNo(@Valid @RequestBody ChangeOtherDetailsReq request) {

        return ResponseEntity.ok(userService.updateContactNo(request));
    }

    @PatchMapping("/address")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddAddressReq request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(request));
    }

    @PatchMapping("/deleteUser")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> deleteUser() {
        return ResponseEntity.ok(userService.deleteUser());
    }


}
