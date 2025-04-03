package com.example.shop3.service;

import com.example.shop3.dto.ActiveCartDTO;
import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.model.Cart; // اضافه شد
import com.example.shop3.model.CartStatus;
import com.example.shop3.model.Item;
import com.example.shop3.model.User;
import com.example.shop3.repository.CartRepository;
import com.example.shop3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;        // <-- Import Page
import org.springframework.data.domain.Pageable;    // <-- Import Pageable
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- اضافه شد

import java.math.BigDecimal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Optional;
// import java.util.List; // دیگر نیازی به این نیست
// import java.util.stream.Collectors; // برای map کردن Page استفاده می‌شود

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true) // <-- تراکنش فقط خواندنی برای بهینگی
    public Page<CartSummaryDTO> getCartsByUserId(Long userId, Pageable pageable) { // <-- ورودی Pageable و خروجی Page

        // 1. گرفتن Page<Cart> از ریپازیتوری
        Page<Cart> cartPage = cartRepository.findCartsWithDetailsByUserId(userId, pageable);

        // 2. تبدیل Page<Cart> به Page<CartSummaryDTO>
        // متد map خود Page این کار را برای آیتم‌های صفحه فعلی انجام می‌دهد
        return cartPage.map(cart -> { // <-- استفاده از map خود Page
            // محاسبه قیمت کل برای این سبد (کد قبلی شما عالی بود)
            BigDecimal totalPrice = cart.getItems().stream()
                    .filter(item -> item.getBook() != null && item.getBook().getPrice() != null)
                    .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ساخت CartSummaryDTO
            return CartSummaryDTO.builder() // <-- استفاده از بیلدر خواناتر است
                    .id(cart.getId())
                    .createdAt(cart.getCreatedAt())
                    .status(cart.getStatus())
                    .totalPrice(totalPrice)
                    .build();
        });
    }
    @Transactional
    public ActiveCartDTO  getActiveCartByUserId(Long userId) {
        Cart cart =cartRepository.findCartByStatusAndUser_Id(CartStatus.ACTIVE,userId)
                .orElseGet(() -> createActiveCart(userId));
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (cart.getItems()!=null && !cart.getItems().isEmpty()) {
            for(Item item : cart.getItems()) {
                totalPrice = totalPrice.add(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        return ActiveCartDTO.builder().
                items(new ArrayList<>(cart.getItems()))
                .price(totalPrice)
                .build();
    }
    @Transactional
    protected Cart createActiveCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        Cart cart= Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        return cartRepository.save(cart);
    }//createActiveCart

}//class