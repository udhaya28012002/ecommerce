package org.learning.ecommerceapp.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public String getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {

            throw new IllegalArgumentException("User is not authenticated");
        }

        System.out.println("Authorization : " + authentication.getAuthorities().stream().toList());

        return authentication.getName();
    }

}
