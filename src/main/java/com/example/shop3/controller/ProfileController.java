package com.example.shop3.controller;

import com.example.shop3.config.SecurityUserDetails; // کلاس UserDetails سفارشی شما
import com.example.shop3.dto.UserResponse;
import com.example.shop3.service.ProfileService; // سرویس جدید یا سرویس User قبلی که متد را دارد
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize; // برای کنترل دسترسی
import org.springframework.security.core.annotation.AuthenticationPrincipal; // برای گرفتن کاربر لاگین کرده
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile") // همه URL های این کنترلر با /profile شروع می شوند
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService; // تزریق سرویس

    // فقط کاربران احراز هویت شده می توانند به این متد دسترسی داشته باشند
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String showUserProfile(
            // با استفاده از این انوتیشن، Spring Security کاربر لاگین کرده فعلی
            // (از نوع SecurityUserDetails که خودمان ساختیم) را به متد پاس می دهد.
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            Model model) { // Model برای ارسال داده به صفحه Thymeleaf

        // از currentUser، آیدی کاربر را می گیریم
        Long userId = currentUser.getUserEntity().getId(); // فرض: متد getUserEntity() در SecurityUserDetails وجود دارد

        // سرویس را برای گرفتن اطلاعات پروفایل فراخوانی می کنیم
        UserResponse userProfileData = profileService.getUserProfile(userId);

        // داده های گرفته شده را به Model اضافه می کنیم تا در Thymeleaf قابل دسترس باشد
        // نام "userProfile" کلیدی است که در Thymeleaf استفاده خواهیم کرد
        model.addAttribute("userProfile", userProfileData);

        // نام فایل HTML در پوشه templates (بدون پسوند .html) را برمی گردانیم
        return "profile"; // -> اشاره به src/main/resources/templates/profile.html
    }
}