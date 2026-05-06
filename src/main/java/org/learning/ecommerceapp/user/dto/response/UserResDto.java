package org.learning.ecommerceapp.user.dto.response;

import org.learning.ecommerceapp.user.entity.Address;

import java.util.List;


public class UserResDto {

    private String name;

    private String userName;

    private String emailId;

    private String contactNo;

    private List<Address> address;

    public UserResDto(String name, String userName, String emailId, String contactNo, List<Address> address) {
        this.name = name;
        this.userName = userName;
        this.emailId = emailId;
        this.contactNo = contactNo;
        this.address = address;
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
}
