package org.learning.ecommerceapp.discount.dto;

import java.util.HashMap;
import java.util.Map;

public class DisplayCouponsRes {

    private Map<String, CouponDetailsRes> availableCoupons;

    public Map<String, CouponDetailsRes> getAvailableCoupons() {
        return availableCoupons;
    }

    public void setAvailableCoupons(Map<String, CouponDetailsRes> availableCoupons) {
        this.availableCoupons = availableCoupons;
    }
}
