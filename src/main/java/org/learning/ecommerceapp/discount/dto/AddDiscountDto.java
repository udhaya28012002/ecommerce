package org.learning.ecommerceapp.discount.dto;

import org.learning.ecommerceapp.discount.entity.DiscountType;

public class AddDiscountDto {

    private String couponCode;

    private String description;

    private DiscountType discountType;

    private long discountValue;

    private long minAmtOrder;

    private long maxDiscountAmount;

    private int validityInMonths;

    private int usageLimit;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public long getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(long discountValue) {
        this.discountValue = discountValue;
    }

    public long getMinAmtOrder() {
        return minAmtOrder;
    }

    public void setMinAmtOrder(long minAmtOrder) {
        this.minAmtOrder = minAmtOrder;
    }

    public long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(long maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public int getValidityInMonths() {
        return validityInMonths;
    }

    public void setValidityInMonths(int validityInMonths) {
        this.validityInMonths = validityInMonths;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
