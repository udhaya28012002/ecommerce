package org.learning.ecommerceapp.user.repository;

import org.learning.ecommerceapp.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users, Long> {

    boolean existsByUserName(String userName);

    boolean existsByEmailId(String emailId);

    boolean existsByContactNo(String contactNo);

    Users findByUserName(String userName);

    Users findByEmailId(String emailId);

    Users findByContactNo(String contactNo);

}