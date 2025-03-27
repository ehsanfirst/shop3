//package com.example.shop3;
//
//import com.example.shop3.model.Role;
//import com.example.shop3.model.User;
//import com.example.shop3.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//
//public class CreateFirstUser implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public void run(String... args) {
//        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        String password = passwordEncoder.encode("1234");
//
//        User user = User.builder()
//                .username("user1")
//                .password(password)
//                .email("user1@email.com")
//                .role(Role.USER)
//                .build();
//
//        userRepository.save(user);
//    }
//}