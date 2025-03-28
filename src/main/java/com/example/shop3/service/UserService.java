package com.example.shop3.service;

import com.example.shop3.config.SecurityUserDetails;
import com.example.shop3.dto.UserRequest;
import com.example.shop3.model.Role;
import com.example.shop3.model.User;
import com.example.shop3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user.not.found : " + username));

        return new SecurityUserDetails(user);

    }

    public void registerUser(UserRequest userRequest, String avatarUrl) throws Exception { // امضا تغییر کرد
        // بررسی تکراری بودن (می‌تواند اینجا یا با Exception Handling از Repository انجام شود)
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new Exception("Username already exists"); // یا Exception سفارشی
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new Exception("Email already exists"); // یا Exception سفارشی
        }

        // --- تبدیل DTO به Entity ---
        User newUserEntity = new User();
        newUserEntity.setUsername(userRequest.getUsername());
        newUserEntity.setEmail(userRequest.getEmail());
        newUserEntity.setPassword(passwordEncoder.encode(userRequest.getPassword())); // انکود کردن پسورد
        if (avatarUrl != null) {
            newUserEntity.setAvatar(avatarUrl);
        }
        newUserEntity.setRole(Role.USER); // پیش‌فرض
        newUserEntity.setEnabled(true);  // پیش‌فرض
        // createdAt , updatedAt معمولا خودکار توسط JPA ست می شوند

        // --- ذخیره Entity در دیتابیس ---
        try {
            userRepository.save(newUserEntity);
        } catch (DataIntegrityViolationException e) {
            // ممکن است باز هم خطای تکراری بودن رخ دهد (race condition)
            // می‌توانید Exception را دوباره throw کنید یا مدیریت دقیق‌تری انجام دهید
            System.err.println("Data integrity violation during save: " + e.getMessage());
            // تبدیل به یک Exception قابل فهم‌تر برای کنترلر
            throw new Exception("Registration failed due to data conflict.", e);
        }
    }

}