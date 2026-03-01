package com.jwtAuthentication.jwt.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private final User user;
    public UserPrincipal(User user) {
        this.user = user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the actual user's role instead of hardcoded "USER"
        Role userRole = user.getRole();
        String role = (userRole != null) ? userRole.name() : "USER";
        // Spring Security requires "ROLE_" prefix for hasRole() checks
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
