package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.model.UserPrincipal;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
       User user=userRepository.findByEmail(email);
        System.out.println(user);
        if(email==null){
            throw new UsernameNotFoundException("User not found with username: " + email);
        }
        return new UserPrincipal(user);
    }
}
