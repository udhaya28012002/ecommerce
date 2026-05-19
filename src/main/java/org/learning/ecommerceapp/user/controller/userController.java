package org.learning.ecommerceapp.user.controller;

import jakarta.validation.Valid;
import org.learning.ecommerceapp.user.dto.request.*;
import org.learning.ecommerceapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class userController {

    private final UserService userService;

    @Autowired
    public userController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReqDto loginReq) {
        return ResponseEntity.ok(userService.login(loginReq));
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserReqDto userReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userReqDto, false));
    }

    @PostMapping("/getUsers")
    public ResponseEntity<?> getUsers(@Valid @RequestBody LoginReqDto loginReq) {
        return ResponseEntity.ok(userService.getAllUsers(loginReq));
    }

    @PostMapping("/getUser")
    public ResponseEntity<?> getUser(@Valid @RequestBody LoginReqDto loginReq) {

        return ResponseEntity.ok(userService.getUser(loginReq));
    }

    @PatchMapping("/{userName}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String userName,
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(userName, request));
    }

    @PatchMapping("/{userName}/contactNo")
    public ResponseEntity<?> changeContactNo(
            @PathVariable String userName,
            @Valid @RequestBody ChangeOtherDetailsReq request) {

        return ResponseEntity.ok(userService.updateContactNo(userName, request));
    }

    @PatchMapping("/{userName}/address")
    public ResponseEntity<?> addAddress(
            @PathVariable String userName,
            @Valid @RequestBody AddAddressReq request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(userName, request));
    }


}
