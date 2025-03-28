package com.example.shop3.config; // یا هر پکیج مناسب دیگر

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // بهترین کار استفاده از DelegatingPasswordEncoder است
        // که به طور پیش فرض از Bcrypt استفاده می کند و امکان ارتقا در آینده را می دهد
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // یا اگر فقط Bcrypt می خواهید:
        // return new BCryptPasswordEncoder();
    }
}