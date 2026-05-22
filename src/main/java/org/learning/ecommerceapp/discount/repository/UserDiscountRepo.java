package org.learning.ecommerceapp.discount.repository;

import org.learning.ecommerceapp.discount.entity.DiscountOnUsers;
import org.learning.ecommerceapp.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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


    boolean existsByUsersAndCouponCode(Users users, String couponCode);

    List<Long> findUserIdsByCouponCode(String couponCode);
}
