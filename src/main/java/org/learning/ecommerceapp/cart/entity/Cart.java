package org.learning.ecommerceapp.cart.entity;

import jakarta.persistence.*;
import org.learning.ecommerceapp.user.entity.Users;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private long cartId;

    @OneToOne
    private Users users;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItems> cartItemsList =  new ArrayList<>();;

    public List<CartItems> getCartItemsList() {
        return cartItemsList;
    }

    public void setCartItemsList(List<CartItems> cartItemsList) {
        this.cartItemsList = cartItemsList;
    }

    public Users getUser() {
        return users;
    }

    public void setUser(Users users) {
        this.users = users;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }
}
