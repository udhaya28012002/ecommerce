package org.learning.ecommerceapp.user.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.discount.service.DiscountService;
import org.learning.ecommerceapp.user.dto.request.UserCreationDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final UserService userService;
    private final DiscountService discountService;

    public RegistrationService(UserService userService, DiscountService discountService) {
        this.userService = userService;
        this.discountService = discountService;
    }

    @Transactional
    public UserResDto registerUser(UserCreationDto dto, boolean isAdmin) {

        log.info("Registering new user with username: {}", dto.getUserName());

        Users user = userService.createUser(dto, isAdmin);

        log.info("User created successfully: {}", user.getUserName());

        discountService.assignWelcomeCoupon(user);

        log.info("Welcome coupon assigned to user: {}", user.getUserName());

        return new UserResDto(user.getName(), user.getUserName(), user.getEmailId(), user.getEmailId(), user.getAddress());
    }
}