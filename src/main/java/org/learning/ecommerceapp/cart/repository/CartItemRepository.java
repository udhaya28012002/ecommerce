package org.learning.ecommerceapp.cart.repository;

import org.learning.ecommerceapp.cart.entity.Cart;
import org.learning.ecommerceapp.cart.entity.CartItems;
import org.learning.ecommerceapp.products.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Long> {

    CartItems findByCartAndProducts(Cart cart, Products products);

    void deleteByCart(Cart cart);

    boolean deleteByCartAndProducts_ProductId(Cart cart, long productId);

    boolean existsByCartAndProducts_ProductId(Cart cart, long productId);

}
