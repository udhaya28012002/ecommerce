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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DiscountService.class);

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

        log.debug("Assigning welcome coupon for user: {}", user.getUsername());

        LocalDateTime registrationTime = user.getCreatedAt();
        if (registrationTime.isBefore(LocalDateTime.now().minusDays(1))) {

            log.warn("Welcome coupon not applicable for user: {}", user.getUsername());

            throw new DiscountNotApplicable("Welcome Gift is only new users.");
        }

        if (userDiscountRepo.existsByUsersAndCouponCode(user, NEW_USER_COUPON)) {

            log.warn("Welcome coupon already assigned for user: {}", user.getUsername());

            throw new DuplicateDiscountException("Discount already exists for this product");
        }

        DiscountOnUsers discountOnUsers = createDiscount(user, NEW_USER_COUPON, "Welcome Gift", DiscountType.FLAT, NEW_USER_COUPON_VALUE, 200, 100, 1, LocalDate.now().plusMonths(6));

        userDiscountRepo.save(discountOnUsers);

        log.info("Welcome coupon assigned successfully for user: {}", user.getUsername());

    }

    @Transactional
    public void assignDiscountToAllUsers(AddDiscountDto dto) {

        log.debug("Assigning coupon to all users. CouponCode: {}", dto.getCouponCode());

        List<Users> allUsers = userService.findAllUsers_ForInternal();

        List<Long> existingUserIds = userDiscountRepo.findUserIdsByCouponCode(dto.getCouponCode());

        List<DiscountOnUsers> discountedUsers = new ArrayList<>();

        LocalDate expiry = LocalDate.now().plusMonths(dto.getValidityInMonths());

        for (Users user : allUsers) {

            if (existingUserIds.contains(user.getId())) {

                log.warn("Coupon already exists for user: {}", user.getUsername());

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

        log.info("Coupons assigned successfully to {} users", discountedUsers.size());
    }

    @Transactional
    public void assignDiscountToEligibleUsers(AddDiscountDto addDiscountDto, double filterPrice) {

        log.debug("Assigning coupon to eligible users. CouponCode: {}, FilterPrice: {}", addDiscountDto.getCouponCode(), filterPrice);

        List<Users> filteredUsers = userDiscountRepo.findEligibleUsers(filterPrice);

        List<Long> existingUserIds = userDiscountRepo.findUserIdsByCouponCode(addDiscountDto.getCouponCode());

        List<DiscountOnUsers> discountedUsers = new ArrayList<>();

        LocalDate expiry = LocalDate.now().plusMonths(addDiscountDto.getValidityInMonths());

        for (Users user : filteredUsers) {

            if (existingUserIds.contains(user.getId())) {

                log.warn("Coupon already assigned for user: {}", user.getUsername());
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

        log.info("Coupons assigned successfully to eligible users");
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

    public void revertCoupon(String coupon) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Reverting coupon: {} for user: {}", coupon, loggedUser);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        int reactivateCoupon = userDiscountRepo.reactivateCoupon(user.getId(), coupon);
        int decrementUsageCount = userDiscountRepo.decrementUsageCount(user.getId(), coupon);

        if (reactivateCoupon == 0 || decrementUsageCount == 0) {
            log.info("Coupon Reverted Failed for {} : {}", user.getUserName(), coupon);
        } else {
            log.info("Coupon Reverted for {} : {}", user.getUserName(), coupon);
        }
    }

    public boolean validateCoupon(String coupon) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Validating coupon: {} for user: {}", coupon, loggedUser);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        boolean valid = userDiscountRepo.existsByUsersAndCouponCode(user, coupon);

        log.info("Coupon validation result for {} : {}", coupon, valid);

        return valid;
    }

    public DisplayCouponsRes displayCoupons() {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Fetching coupons for user: {}", loggedUser);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        if (user.getDiscountOnUsers().isEmpty()) {

            log.warn("No coupons available for user: {}", loggedUser);

            throw new NoCouponAvailable("No applicable coupon available");
        }

        log.info("Coupons fetched successfully for user: {}", loggedUser);

        return buildCouponDetailsRes(user.getDiscountOnUsers());
    }

    public DisplayCouponsRes displayAllCoupons() {

        log.debug("Fetching all coupons");

        Set<DiscountOnUsers> discountOnUsers = new HashSet<>(userDiscountRepo.findAll());

        log.info("Total coupons fetched: {}", discountOnUsers.size());

        return buildCouponDetailsRes(discountOnUsers);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public ApplyCouponResponse applyDiscountByUsers(String couponCode, double purchasePrice) {

        String loggedUser = currentUserService.getLoggedInUser();

        log.debug("Applying coupon. User: {}, CouponCode: {}, PurchasePrice: {}", loggedUser, couponCode, purchasePrice);

        Users user = userService.findByUsername_ForInternal(loggedUser);

        Set<DiscountOnUsers> discountOnUser = user.getDiscountOnUsers();

        Optional<DiscountOnUsers> userDiscount = discountOnUser.stream()
                .filter((discountOnUsers -> discountOnUsers.getCouponCode().equals(couponCode) && discountOnUsers.getActive()))
                .findFirst();

        ApplyCouponResponse applyCouponResponse = new ApplyCouponResponse();

        if (userDiscount.isEmpty()) {

            log.warn("Coupon not available for user: {}", loggedUser);

            return failureResponse(couponCode, purchasePrice);
        }

        DiscountOnUsers discount = userDiscount.get();

        boolean validCoupon = (discount.getActive()
                && (!discount.getEndDate().isBefore(LocalDate.now()))
                && (purchasePrice >= discount.getMinimumOrderAmount().doubleValue())
                && (discount.getUsedCount() < discount.getUsageLimit())
        );

        if (!validCoupon) {

            log.warn("Coupon validation failed for user: {}", loggedUser);

            return failureResponse(couponCode, purchasePrice);
        }

        DiscountType discountType = discount.getDiscountType();

        double discountValue = discountType.equals(DiscountType.FLAT) ? discount.getDiscountValue().doubleValue() : (purchasePrice * discount.getDiscountValue().doubleValue()) / 100;

        double finalPriceAfterDiscounts = (discount.getMaximumDiscountAmount().doubleValue() > discountValue) ? purchasePrice - discountValue : purchasePrice - discount.getMaximumDiscountAmount().doubleValue();

        discount.setUsedCount(discount.getUsedCount() + 1);

        if (discount.getUsedCount() >= discount.getUsageLimit()) {
            discount.setActive(false);

            log.info("Coupon deactivated after reaching usage limit. CouponCode: {}", couponCode);
        }

        applyCouponResponse.setApplied(true);
        applyCouponResponse.setCouponName(couponCode);
        applyCouponResponse.setMessage(discount.getCouponCode() + " code is applied");
        applyCouponResponse.setFinalPrice(finalPriceAfterDiscounts);

        log.info("Coupon applied successfully. User: {}, CouponCode: {}, FinalPrice: {}", loggedUser, couponCode, finalPriceAfterDiscounts);

        return applyCouponResponse;
    }

    private ApplyCouponResponse failureResponse(String couponCode, double purchasePrice) {

        log.warn("Coupon application failed. CouponCode: {}", couponCode);

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
                                        discount.getActive(),
                                        discount.getUsageLimit()
                                ),
                        (existing, duplicate) -> existing
                ));

        displayCouponsRes.setAvailableCoupons(couponDetailsMap);

        System.out.println(displayCouponsRes.getAvailableCoupons());

        return displayCouponsRes;
    }
}
