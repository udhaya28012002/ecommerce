package org.learning.ecommerceapp.user.entity;

import jakarta.persistence.*;
import org.learning.ecommerceapp.order.entity.Orders;
import org.learning.ecommerceapp.user.commons.enums.Role;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    private String name;

    private String userName;

    private String emailId;

    private String contactNo;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Address> address;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Orders> orders;

    public Users(){}

    public Users(String name, String userName, String emailId, String contactNo, List<Address> address, String password, Role role, String status, LocalDateTime createdAt) {
        this.name = name;
        this.userName = userName;
        this.emailId = emailId;
        this.contactNo = contactNo;
        this.address = address;
        this.password = password;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getContactNo() {
        return contactNo;
    }

    public List<Address> getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
