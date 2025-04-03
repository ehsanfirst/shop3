package com.example.shop3.controller;

import com.example.shop3.config.SecurityUserDetails;
import com.example.shop3.dto.ActiveCartDTO;
import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;            // <-- Import Page
import org.springframework.data.domain.Pageable;        // <-- Import Pageable
import org.springframework.data.web.PageableDefault;   // <-- Import PageableDefault
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// import java.util.List; // دیگر نیازی به List نیست

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public String showAllCarts( // اسم متد رو واضح‌تر کردم
                                @AuthenticationPrincipal SecurityUserDetails currentUser,
                                // Pageable رو به عنوان پارامتر اضافه کن
                                // PageableDefault اندازه پیش‌فرض صفحه رو 10 و مرتب‌سازی پیش‌فرض رو تعیین می‌کنه
                                @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                                Model model) {

        Long userId = currentUser.getUserEntity().getId();

        // فراخوانی سرویس با userId و pageable
        Page<CartSummaryDTO> cartPage = cartService.getCartsByUserId(userId, pageable);

        // اضافه کردن Page به Model به جای List
        model.addAttribute("cartPage", cartPage); // <-- نام attribute رو هم به cartPage تغییر دادم

        // اضافه کردن اطلاعات کاربر (اگر در صفحه لازم دارید)
        // model.addAttribute("currentUser", currentUser.getUserEntity());

        return "carts/showCarts"; // نام تمپلیت شما
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public String activeCart(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            Model model
    ){
        ActiveCartDTO activeCartDTO=cartService.getActiveCartByUserId(userDetails.getUserEntity().getId());
        model.addAttribute("cart",activeCartDTO);
        return "carts/activeCart";
    }


}