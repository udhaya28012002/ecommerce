package org.learning.ecommerceapp.user.service;

import org.learning.ecommerceapp.user.enums.Role;
import org.learning.ecommerceapp.user.dto.request.*;
import org.learning.ecommerceapp.user.dto.response.InfoDto;
import org.learning.ecommerceapp.user.dto.response.UserResDto;
import org.learning.ecommerceapp.user.entity.Address;
import org.learning.ecommerceapp.user.entity.Users;
import org.learning.ecommerceapp.user.enums.UserStatus;
import org.learning.ecommerceapp.user.exception.*;
import org.learning.ecommerceapp.user.repository.UserRepo;
import org.learning.ecommerceapp.util.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final CurrentUserService currentUserService;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, CurrentUserService currentUserService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername_ForInternal(username);
    }

    /*public LoginResDto login(LoginReqDto loginReq) {

        Users user = userRepo.findByUserName(loginReq.getUsername());

        if (user == null || !passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return new LoginResDto("Login successful");
    }*/

    public Users createUser(UserCreationDto dto, boolean isAdmin) {
        Role role = isAdmin ? Role.ROLE_ADMIN : Role.ROLE_CUSTOMER;

        if (isPasswordMismatch(dto.getPassword(), dto.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and Confirm Password must match");
        }

        if (isUsernameAvailable(dto.getUserName()) || isEmailIdAlreadyRegistered(dto.getEmailId()) || isContactNoIsAlreadyRegistered(dto.getContactNo())) {
            throw new ResourceAlreadyExistsException("Username (or) Email (or) Contact No is already registered");
        }

        ArrayList<Address> addresses = new ArrayList<>();

        if (dto.getAddress() == null) {
            throw new AddressNotFoundException("Address cannot be null");
        }

        Address address = dto.getAddress();
        address.setDefault(true);
        addresses.add(address);

        Users user = new Users(
                dto.getName(),
                dto.getUserName(),
                dto.getEmailId(),
                dto.getContactNo(),
                addresses,
                passwordEncoder.encode(dto.getPassword()),
                role,
                UserStatus.ACTIVE.name(),
                LocalDateTime.now()
        );

        return userRepo.save(user);
    }

    public Page<UserResDto> getAllUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Users> allUsers = userRepo.findAll(pageable);

        if (allUsers.isEmpty()) {
            throw new ResourceNotFoundException("There are no users in the database");
        }

        return allUsers.map(user -> new UserResDto(
                user.getName(),
                user.getUserName(),
                user.getEmailId(),
                user.getContactNo(),
                user.getAddress()
        ));
    }

    /*public UserResDto getUser(LoginReqDto loginReq) {

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
    }*/

    public List<Users> findAllUsers_ForInternal(){
        return userRepo.findAll();
    }

    public Users findByUsername_ForInternal(String username){
        Users user = userRepo.findByUserName(username);

        if (user == null) {
            throw new UsernameNotFoundException("Username not found!");
        }
        if(user.getStatus().equals(UserStatus.DEACTIVATED.name())){
            throw new DisabledException("Account inactive");
        }

        return user;
    }

    public UserResDto getUserDetails() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return populateUserDto(user);
    }

    /*private Users getUserByUserName(String username, String givenPass) {

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
    }*/

    @Transactional
    public InfoDto changePassword(ChangePasswordRequest changePasswordRequest) {

        String loggedUser = currentUserService.getLoggedInUser();

        if (isPasswordMismatch(changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and Confirm Password do not match");
        }

        Users user = userRepo.findByUserName(loggedUser);

        if (user == null || !validatePassword(user, changePasswordRequest.getOldPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if(passwordEncoder.matches(
                changePasswordRequest.getNewPassword(),
                user.getPassword())) {

            throw new PasswordReuseException(
                    "New password cannot be same as old password"
            );
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        return new InfoDto("Password changed successfully");
    }

    @Transactional
    public InfoDto updateContactNo(ChangeOtherDetailsReq changeOtherDetailsReq) {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        user.setContactNo(changeOtherDetailsReq.getChangeField());

        return new InfoDto("ContactNo changed successfully");
    }

    @Transactional
    public InfoDto addAddress(AddAddressReq addAddressReq) {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        user.getAddress().forEach(addr -> addr.setDefault(false));

        Address newAddress = new Address();
        newAddress.setCity(addAddressReq.getCity());
        newAddress.setPincode(addAddressReq.getPincode());
        newAddress.setState(addAddressReq.getState());
        newAddress.setStreet(addAddressReq.getStreet());
        newAddress.setDefault(true);

        if (user.getAddress() == null) {
            user.setAddress(new ArrayList<>());
        }

        user.getAddress().add(newAddress);

        return new InfoDto("New Address has been added successfully");
    }

    @Transactional
    public InfoDto deleteUser() {

        String loggedUser = currentUserService.getLoggedInUser();

        Users user = userRepo.findByUserName(loggedUser);

        if (user == null) {
            throw new InvalidCredentialsException("UserNotFound");
        }

        user.setStatus(UserStatus.DEACTIVATED.name());

        SecurityContextHolder.clearContext();

        return new InfoDto("Account deactivated successfully");
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

    private boolean isPasswordMismatch(String password, String confirmPassword) {
        return !password.equalsIgnoreCase(confirmPassword);
    }

    private boolean validatePassword(Users user, String givenPassword) {
        return passwordEncoder.matches(givenPassword, user.getPassword());
    }

    private boolean isAdmin(LoginReqDto loginReq) {
        Users user = userRepo.findByUserName(loginReq.getUsername());

        if (!validatePassword(user, loginReq.getPassword())) {
            return false;
        }

        return user.getRole() == Role.ROLE_ADMIN;
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
