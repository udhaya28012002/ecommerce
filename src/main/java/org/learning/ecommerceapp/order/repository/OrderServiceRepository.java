package org.learning.ecommerceapp.order.repository;

import org.hibernate.query.Order;
import org.learning.ecommerceapp.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderServiceRepository extends JpaRepository<Orders, Long> {

    Orders findByOrderNumber(String orderNumber);

    List<Orders> findByUsers_UserName(String userName);

    List<Orders> findByUsers_EmailId(String emailId);

    List<Orders> findByUsers_ContactNo(String contactNo);

}
