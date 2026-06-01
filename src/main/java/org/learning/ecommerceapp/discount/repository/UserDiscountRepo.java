package org.learning.ecommerceapp.discount.repository;

import org.learning.ecommerceapp.discount.entity.DiscountOnUsers;
import org.learning.ecommerceapp.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserDiscountRepo extends JpaRepository<DiscountOnUsers, Long> {

    @Query("""
            SELECT u
            FROM Users u
            JOIN u.orders o
            JOIN o.orderItemsList oi
            GROUP BY u
            HAVING SUM(oi.totalPrice) >= :amount
            """)
    List<Users> findEligibleUsers(double amount);

    @Modifying
    @Query("""
    UPDATE DiscountOnUsers dou
    SET dou.active = true
    WHERE dou.users.id = :userId
      AND dou.couponCode = :couponCode
    """)
    int reactivateCoupon(@Param("userId") Long userId, @Param("couponCode") String couponCode);


    @Modifying
    @Query("""
    UPDATE DiscountOnUsers d
    SET d.usedCount = d.usedCount - 1
    WHERE d.couponCode = :couponCode
      AND d.usedCount > 0
      AND d.users.id = :userId
    """)
    int decrementUsageCount(@Param("userId") Long userId, @Param("couponCode") String couponCode);


    boolean existsByUsersAndCouponCode(Users users, String couponCode);

    @Query("""
                SELECT d.users.id
                FROM DiscountOnUsers d
                WHERE d.couponCode = :couponCode
            """)
    List<Long> findUserIdsByCouponCode(String couponCode);
}
