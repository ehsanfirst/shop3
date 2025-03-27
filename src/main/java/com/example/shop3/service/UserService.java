package com.example.shop3.service;

import com.example.shop3.config.SecurityUserDetails;
import com.example.shop3.model.User;
import com.example.shop3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("user.not.found : " + username));

        return new SecurityUserDetails(user);

    }
}
