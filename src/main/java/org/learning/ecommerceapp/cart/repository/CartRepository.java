package org.learning.ecommerceapp.cart.repository;

import org.learning.ecommerceapp.cart.entity.Cart;
import org.learning.ecommerceapp.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByUsers(Users users);

}
