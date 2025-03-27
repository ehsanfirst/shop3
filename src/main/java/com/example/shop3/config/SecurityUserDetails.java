package com.example.shop3.config; // یا پکیج مناسب دیگر (مثلا service یا security)

import com.example.shop3.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // برای ساخت لیست تک عضوی

public class SecurityUserDetails implements UserDetails {

    private final User user; // نگهداری نمونه‌ای از User entity

    public SecurityUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // تبدیل Role enum به GrantedAuthority
        // معمولا نقش‌ها با پیشوند ROLE_ همراه هستند
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // اگر نقش‌های بیشتری داشتید، می‌توانید لیست بزرگتری بسازید
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // پسورد را از User entity می‌خواند
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // نام کاربری را از User entity می‌خواند
    }

    // --- متدهای مربوط به وضعیت حساب ---
    // اگر فیلدهای متناظر در User entity دارید، از آن‌ها استفاده کنید
    // در غیر این صورت، می‌توانید true برگردانید (اگر منطق خاصی ندارید)

    @Override
    public boolean isAccountNonExpired() {
        return true; // فعلا فرض می‌کنیم حساب منقضی نمی‌شود
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // فعلا فرض می‌کنیم حساب قفل نمی‌شود
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // فعلا فرض می‌کنیم اعتبار (پسورد) منقضی نمی‌شود
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled(); // وضعیت فعال/غیرفعال بودن را از User entity می‌خواند
    }

    // (اختیاری) متدی برای دسترسی به User entity اصلی، اگر لازم شد
    public User getUserEntity() {
        return user;
    }
}