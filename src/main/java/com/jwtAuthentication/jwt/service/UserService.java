package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.requestDto.SignUpRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.execption.DuplicateResourceException;
import com.jwtAuthentication.jwt.model.Activity;
import com.jwtAuthentication.jwt.model.Role;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@Service
public class UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final ActivityService activityService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       ActivityService activityService) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.activityService = activityService;
    }

    /* ========================= LOGIN ========================= */

    public LoginResponse verifyUser(LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (!authentication.isAuthenticated()) {
                throw new RuntimeException("Authentication failed");
            }

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String role = user.getRole().name();
            String token = jwtService.generateToken(user.getEmail(), role, user.getId());
            
            // Create LoginResponse with full user data
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setRole(role);
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setDob(user.getDob() != null ? user.getDob().toString() : null);
            response.setCountry(user.getCountry());
            response.setPhoneNo(user.getPhoneNo());
            response.setBio(user.getBio());
            response.setImage(user.getImage());
            
            return response;

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }


    /* ========================= FILE ========================= */

    public InputStream getResources(String path, String fileName) throws FileNotFoundException {
        return new FileInputStream(path + File.separator + fileName);
    }

    /* ========================= REGISTER ========================= */

    public void saveUser(SignUpRequestDto request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        
        // Log activity
        activityService.logActivity(
            Activity.ActivityType.USER_REGISTERED,
            "New user '" + savedUser.getUsername() + "' registered to the platform",
            savedUser.getUsername(),
            Long.valueOf(savedUser.getId()),
            savedUser.getEmail()
        );
    }


    /* ========================= DELETE USER ========================= */

    public String deleteUser(int id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔐 ADMIN protection
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Admin user cannot be deleted");
        }

        userRepository.delete(user);
        return "User deleted successfully";
    }

    /* ========================= UPDATE USER ========================= */

    public User updateUser(User requestUser, int id) {

        // 🔐 Logged-in user
        String loggedInEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        // 🔐 USER can update only self
        if (loggedInUser.getRole() == Role.USER && loggedInUser.getId() != id) {
            throw new RuntimeException("Access denied");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Allowed fields only
        if (requestUser.getUsername() != null && !requestUser.getUsername().isEmpty())
            existingUser.setUsername(requestUser.getUsername());

        // Check for email uniqueness before updating
        if (requestUser.getEmail() != null && !requestUser.getEmail().isEmpty()) {
            if (!requestUser.getEmail().equals(existingUser.getEmail())) {
                // Email is being changed, check if new email already exists
                if (userRepository.findByEmail(requestUser.getEmail()).isPresent()) {
                    throw new RuntimeException("Email already exists");
                }
            }
            existingUser.setEmail(requestUser.getEmail());
        }

        if (requestUser.getFirstName() != null && !requestUser.getFirstName().isEmpty())
            existingUser.setFirstName(requestUser.getFirstName());

        if (requestUser.getLastName() != null && !requestUser.getLastName().isEmpty())
            existingUser.setLastName(requestUser.getLastName());

        if (requestUser.getCountry() != null && !requestUser.getCountry().isEmpty())
            existingUser.setCountry(requestUser.getCountry());

        if (requestUser.getPhoneNo() != null && !requestUser.getPhoneNo().isEmpty())
            existingUser.setPhoneNo(requestUser.getPhoneNo());

        if (requestUser.getDob() != null)
            existingUser.setDob(requestUser.getDob());

        if (requestUser.getBio() != null)
            existingUser.setBio(requestUser.getBio());

        if (requestUser.getImage() != null)
            existingUser.setImage(requestUser.getImage());

        // 🔐 Password update (optional)
        if (requestUser.getPassword() != null && !requestUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(requestUser.getPassword()));
        }

        // ❌ ROLE UPDATE REMOVED (VERY IMPORTANT)

        return userRepository.save(existingUser);
    }

    /* ========================= GET USER ========================= */

    public User getUser(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /* ========================= GET ALL USERS ========================= */

    public List<User> findAllUsers() {
        return userRepository.findByRole(Role.USER); // Admin excluded
    }
}
