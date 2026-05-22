package org.learning.ecommerceapp.discount.service;

import org.learning.ecommerceapp.discount.dto.AddDiscountDto;
import org.learning.ecommerceapp.discount.dto.ApplyCouponResponse;
import org.learning.ecommerceapp.discount.dto.CouponDetailsRes;
import org.learning.ecommerceapp.discount.dto.DisplayCouponsRes;
import org.learning.ecommerceapp.discount.entity.DiscountOnUsers;
import org.learning.ecommerceapp.discount.entity.DiscountType;
import org.learning.ecommerceapp.discount.exception.DiscountNotApplicable;
import org.learning.ecommerceapp.discount.exception.DuplicateDiscountException;
import org.learning.ecommerceapp.discount.exception.NoCouponAvailable;
import org.learning.ecommerceapp.discount.repository.GlobalDiscountRepo;
import org.learning.ecommerceapp.discount.repository.UserDiscountRepo;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.service.UserService;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final GlobalDiscountRepo globalDiscountRepo;
    private final UserDiscountRepo userDiscountRepo;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    private final String NEW_USER_COUPON = "WelcomeGift";
    private final long NEW_USER_COUPON_VALUE = 100;

    public DiscountService(GlobalDiscountRepo globalDiscountRepo, UserDiscountRepo userDiscountRepo, CurrentUserService currentUserService, UserService userService) {
        this.globalDiscountRepo = globalDiscountRepo;
        this.userDiscountRepo = userDiscountRepo;
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    public void assignWelcomeCoupon(Users user) {

        LocalDateTime registrationTime = user.getCreatedAt();
        if (registrationTime.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new DiscountNotApplicable("Welcome Gift is only new users.");
        }

        if (userDiscountRepo.existsByUsersAndCouponCode(user, NEW_USER_COUPON)) {
            throw new DuplicateDiscountException("Gift Coupon has been already added");
        }

        DiscountOnUsers discountOnUsers = createDiscount(user, NEW_USER_COUPON, "Welcome Gift", DiscountType.FLAT, NEW_USER_COUPON_VALUE, 200, 100, 1, LocalDate.now().plusMonths(6));

        userDiscountRepo.save(discountOnUsers);

    }

    @Transactional
    public void assignDiscountToAllUsers(AddDiscountDto dto) {

        List<Users> allUsers = userService.findAllUsers_ForInternal();

        List<Long> existingUserIds = userDiscountRepo.findUserIdsByCouponCode(dto.getCouponCode());

        List<DiscountOnUsers> discountedUsers = new ArrayList<>();

        LocalDate expiry = LocalDate.now().plusMonths(dto.getValidityInMonths());

        for (Users user : allUsers) {

            if (existingUserIds.contains(user.getId())) {
                continue;
            }

            discountedUsers.add(createDiscount(
                    user,
                    dto.getCouponCode(),
                    dto.getDescription(),
                    dto.getDiscountType(),
                    dto.getDiscountValue(),
                    dto.getMinAmtOrder(),
                    dto.getMaxDiscountAmount(),
                    dto.getUsageLimit(),
                    expiry
            ));
        }

        userDiscountRepo.saveAll(discountedUsers);
    }

    @Transactional
    public void assignDiscountToEligibleUsers(AddDiscountDto addDiscountDto, double filterPrice) {

        List<Users> filteredUsers = userDiscountRepo.findEligibleUsers(filterPrice);

        List<Long> existingUserIds = userDiscountRepo.findUserIdsByCouponCode(addDiscountDto.getCouponCode());

        List<DiscountOnUsers> discountedUsers = new ArrayList<>();

        LocalDate expiry = LocalDate.now().plusMonths(addDiscountDto.getValidityInMonths());

        for (Users user : filteredUsers) {

            if (existingUserIds.contains(user.getId())) {
                continue;
            }

            DiscountOnUsers discountOnUsers = createDiscount(
                    user,
                    addDiscountDto.getCouponCode(),
                    addDiscountDto.getDescription(),
                    addDiscountDto.getDiscountType(),
                    addDiscountDto.getDiscountValue(),
                    addDiscountDto.getMinAmtOrder(),
                    addDiscountDto.getMaxDiscountAmount(),
                    addDiscountDto.getUsageLimit(),
                    expiry
            );

            discountedUsers.add(discountOnUsers);

        }
        userDiscountRepo.saveAll(discountedUsers);

    }

    private DiscountOnUsers createDiscount(
            Users user,
            String code,
            String description,
            DiscountType discountType,
            long discountValue,
            double minOrderAmount,
            double maxDiscountAmount,
            int usageLimit,
            LocalDate endDate
    ) {
        return new DiscountOnUsers(
                code,
                description,
                discountType,
                BigDecimal.valueOf(discountValue),
                BigDecimal.valueOf(minOrderAmount),
                BigDecimal.valueOf(maxDiscountAmount),
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                usageLimit,
                0,
                true,
                user
        );
    }

    public boolean validateCoupon(String coupon) {

        String loggedUser = currentUserService.getLoggedInUser();
        Users user = userService.findByUsername_ForInternal(loggedUser);

        return userDiscountRepo.existsByUsersAndCouponCode(user, coupon);
    }

    public DisplayCouponsRes displayCoupons() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userService.findByUsername_ForInternal(loggedUser);

        if (user.getDiscountOnUsers().isEmpty()) {
            throw new NoCouponAvailable("No Coupons are available");
        }

        return buildCouponDetailsRes(user.getDiscountOnUsers());
    }

    public DisplayCouponsRes displayAllCoupons() {

        Set<DiscountOnUsers> discountOnUsers = new HashSet<>(userDiscountRepo.findAll());

        return buildCouponDetailsRes(discountOnUsers);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public ApplyCouponResponse applyDiscountByUsers(String couponCode, double purchasePrice) {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Set<DiscountOnUsers> discountOnUser = user.getDiscountOnUsers();

        Optional<DiscountOnUsers> userDiscount = discountOnUser.stream()
                .filter((discountOnUsers -> discountOnUsers.getCouponCode().equals(couponCode) && discountOnUsers.getActive()))
                .findFirst();

        ApplyCouponResponse applyCouponResponse = new ApplyCouponResponse();

        if (userDiscount.isEmpty()) {
            return failureResponse(couponCode, purchasePrice);
        }

        DiscountOnUsers discount = userDiscount.get();

        if (!(discount.getActive()
                && (!discount.getEndDate().isBefore(LocalDate.now()))
                && (purchasePrice >= discount.getMinimumOrderAmount().doubleValue())
                && (discount.getUsedCount() < discount.getUsageLimit())
        )) {
            return failureResponse(couponCode, purchasePrice);
        }

        DiscountType discountType = discount.getDiscountType();

        double discountValue = discountType.equals(DiscountType.FLAT) ? discount.getDiscountValue().doubleValue() : (purchasePrice * discount.getDiscountValue().doubleValue()) / 100;

        double finalPriceAfterDiscounts = (discount.getMaximumDiscountAmount().doubleValue() > discountValue) ? purchasePrice - discountValue : purchasePrice - discount.getMaximumDiscountAmount().doubleValue();

        discount.setUsedCount(discount.getUsedCount() + 1);

        if (discount.getUsedCount() >= discount.getUsageLimit()) {
            discount.setActive(false);
        }

        applyCouponResponse.setApplied(true);
        applyCouponResponse.setCouponName(couponCode);
        applyCouponResponse.setMessage(discount.getCouponCode() + " code is applied");
        applyCouponResponse.setFinalPrice(finalPriceAfterDiscounts);

        return applyCouponResponse;
    }

    private ApplyCouponResponse failureResponse(String couponCode, double purchasePrice) {

        ApplyCouponResponse response = new ApplyCouponResponse();

        response.setCouponName("No Coupon is used");
        response.setApplied(false);
        response.setMessage("Coupon Code Is Not Available");
        response.setFinalPrice(purchasePrice);

        return response;
    }

    private DisplayCouponsRes buildCouponDetailsRes(Set<DiscountOnUsers> discounts) {
        DisplayCouponsRes displayCouponsRes = new DisplayCouponsRes();

        Map<String, CouponDetailsRes> couponDetailsMap = discounts.stream()
                .collect(Collectors.toMap(
                        discount -> discount.getCouponCode(),
                        discount ->
                                new CouponDetailsRes(
                                        discount.getDescription(),
                                        discount.getDiscountType(),
                                        discount.getDiscountValue(),
                                        discount.getMinimumOrderAmount(),
                                        discount.getMaximumDiscountAmount(),
                                        discount.getEndDate(),
                                        discount.getActive()
                                ),
                        (existing, duplicate) -> existing
                ));

        displayCouponsRes.setAvailableCoupons(couponDetailsMap);
        return displayCouponsRes;
    }
}
