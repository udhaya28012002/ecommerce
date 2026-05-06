package org.learning.ecommerceapp.user.service;

import org.learning.ecommerceapp.user.dto.request.*;
import org.learning.ecommerceapp.user.dto.response.InfoDto;
import org.learning.ecommerceapp.user.dto.response.LoginResDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.entity.Address;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.exception.*;
import org.learning.ecommerceapp.user.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResDto login(LoginReqDto loginReq) {

        Users user = userRepo.findByUserName(loginReq.getUsername());

        if (user == null || !passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return new LoginResDto("Login successful");
    }

    public UserResDto createUser(UserReqDto dto, boolean isAdmin) {
        String role = isAdmin ? "ADMIN" : "USER";

        if (isPasswordMatch(dto.getPassword(), dto.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and Confirm Password do not match");
        }

        if (isUsernameAvailable(dto.getUserName()) || isEmailIdAlreadyRegistered(dto.getEmailId()) || isContactNoIsAlreadyRegistered(dto.getContactNo())) {
            throw new ResourceAlreadyExistsException("Username (or) Email (or) Contact No is already registered");
        }

        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(dto.getAddress());
        Users user = new Users(
                dto.getName(),
                dto.getUserName(),
                dto.getEmailId(),
                dto.getContactNo(),
                addresses,
                passwordEncoder.encode(dto.getPassword()),
                role,
                "active",
                LocalDateTime.now()
        );

        Users insertedUser = userRepo.save(user);

        return new UserResDto(insertedUser.getName(), insertedUser.getUserName(), insertedUser.getEmailId(), insertedUser.getContactNo(), insertedUser.getAddress());
    }

    public List<UserResDto> getUsers(LoginReqDto loginReq) {

        if (!isAdmin(loginReq)) {
            throw new AccessDeniedException("Admin access required");
        }

        List<Users> allUsers = userRepo.findAll();

        if (allUsers.isEmpty()) {
            throw new ResourceNotFoundException("There is no users in the database");
        }

        return allUsers.stream()
                .map(user -> new UserResDto(
                        user.getName(),
                        user.getUserName(),
                        user.getEmailId(),
                        user.getContactNo(),
                        user.getAddress()
                )).toList();
    }

    public UserResDto getUser(LoginReqDto loginReq) {

        if (loginReq.getLoginMethod() == null || loginReq.getLoginMethod().isBlank()) {
            throw new ResourceNotFoundException("Login Method should be defined");
        }

        if (loginReq.getLoginMethod().equalsIgnoreCase("EMAILID")) {
            String email = loginReq.getUsername();
            if (email != null) {
                return populateUserDto(getUserByEmailId(email, loginReq.getPassword()));
            }
        } else if (loginReq.getLoginMethod().equalsIgnoreCase("CONTACTNO")) {
            String contactNo = loginReq.getUsername();
            if (contactNo != null) {
                return populateUserDto(getUserByContactNo(contactNo, loginReq.getPassword()));
            }
        } else if (loginReq.getLoginMethod().equalsIgnoreCase("USERNAME")) {
            String username = loginReq.getUsername();
            if (username != null) {
                return populateUserDto(getUserByUserName(username, loginReq.getPassword()));
            }
        }
        throw new IllegalArgumentException("No filter provided");
    }


    private Users getUserByUserName(String username, String givenPass) {

        Users user = userRepo.findByUserName(username);

        if (user == null || !validatePassword(user, givenPass)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return user;
    }

    private Users getUserByEmailId(String emailId, String givenPass) {

        Users user = userRepo.findByEmailId(emailId);

        if (user == null || !validatePassword(user, givenPass)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return user;
    }

    private Users getUserByContactNo(String contactNo, String givenPass) {

        Users user = userRepo.findByContactNo(contactNo);

        if (user == null || !validatePassword(user, givenPass)) {
            throw new InvalidCredentialsException("Invalid contactNo or password");
        }

        return user;
    }

    @Transactional
    public InfoDto changePassword(String userName, ChangePasswordRequest changePasswordRequest) {

        if (isPasswordMatch(changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and Confirm Password do not match");
        }

        Users user = userRepo.findByUserName(userName);

        if (user == null || !validatePassword(user, changePasswordRequest.getOldPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        return new InfoDto("Password changed successfully");
    }

    @Transactional
    public InfoDto changeContactNo(String userName, ChangeOtherDetailsReq changeOtherDetailsReq) {

        Users user = userRepo.findByUserName(userName);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        user.setContactNo(changeOtherDetailsReq.getChangeField());

        return new InfoDto("ContactNo changed successfully");
    }

    @Transactional
    public InfoDto addAddress(String userName, AddAddressReq addAddressReq) {

        Users user = userRepo.findByUserName(userName);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Address newAddress = new Address();
        newAddress.setCity(addAddressReq.getCity());
        newAddress.setPincode(addAddressReq.getPincode());
        newAddress.setState(addAddressReq.getState());
        newAddress.setStreet(addAddressReq.getStreet());

        if (user.getAddress() == null) {
            user.setAddress(new ArrayList<>());
        }

        user.getAddress().add(newAddress);

        return new InfoDto("New Address has been added successfully");
    }

    public InfoDto deleteUser(String username, String password) {
        Users user = userRepo.findByUserName(username);

        if (user == null || !validatePassword(user, password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        userRepo.deleteById(user.getId());

        return new InfoDto("User deleted successfully");
    }

    private boolean isUsernameAvailable(String username) {
        return userRepo.existsByUserName(username);
    }

    private boolean isEmailIdAlreadyRegistered(String emailId) {
        return userRepo.existsByEmailId(emailId);
    }

    private boolean isContactNoIsAlreadyRegistered(String contactNo) {
        return userRepo.existsByContactNo(contactNo);
    }

    private boolean isPasswordMatch(String password, String confirmPassword) {
        return !password.equalsIgnoreCase(confirmPassword);
    }

    private boolean validatePassword(Users user, String givenPassword) {
        return user.getPassword().equalsIgnoreCase(givenPassword);
    }

    private boolean isAdmin(LoginReqDto loginReq) {
        Users user = userRepo.findByUserName(loginReq.getUsername());

        if (!validatePassword(user, loginReq.getPassword())) {
            return false;
        }

        return user.getRole().equalsIgnoreCase("ADMIN");
    }

    private UserResDto populateUserDto(Users user) {
        return new UserResDto(
                user.getName(),
                user.getUserName(),
                user.getEmailId(),
                user.getContactNo(),
                user.getAddress()
        );
    }

}
