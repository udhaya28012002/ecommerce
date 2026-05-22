package org.learning.ecommerceapp.user.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.discount.service.DiscountService;
import org.learning.ecommerceapp.user.dto.request.UserCreationDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.entity.Users;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserService userService;
    private final DiscountService discountService;

    public RegistrationService(UserService userService, DiscountService discountService) {
        this.userService = userService;
        this.discountService = discountService;
    }

    @Transactional
    public UserResDto registerUser(UserCreationDto dto, boolean isAdmin) {

        Users user = userService.createUser(dto, isAdmin);
        discountService.assignWelcomeCoupon(user);

        return new UserResDto(user.getName(), user.getUserName(), user.getEmailId(), user.getEmailId(), user.getAddress());
    }
}