package com.example.shop3.service;
import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.dto.UserResponse;
import com.example.shop3.model.Cart;
import com.example.shop3.model.User;
import com.example.shop3.repository.BookRepository;
import com.example.shop3.repository.CartRepository;
import com.example.shop3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
// ... other imports

@RequiredArgsConstructor
@Service
public class ProfileService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository; // ریپازیتوری کتاب رو هم تزریق کن

    // متد اصلی برای گرفتن اطلاعات پروفایل
    public UserResponse getUserProfile(Long userId) {

        // --- ۱. پیدا کردن کاربر ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for ID: " + userId));

        // --- ۲. آماده سازی Pageable برای ۵ تای آخر ---
        // صفحه اول (اندیس ۰)، حداکثر ۵ آیتم، مرتب شده بر اساس تاریخ ایجاد نزولی (جدیدترین اول)
        Pageable topFive = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // --- ۳. گرفتن ۵ سبد آخر با جزئیات کامل با *یک* کوئری ---
        Page<Cart> carts = cartRepository.findCartsWithDetailsByUserId(userId, topFive);

        // --- ۴. تبدیل Cart ها به CartSummaryDTO و محاسبه قیمت کل در جاوا ---
        List<CartSummaryDTO> cartSummaries = carts.getContent().stream() // روی لیست سبدها پیمایش کن
                .map(cart -> { // هر سبد رو به یک CartSummaryDTO تبدیل کن
                    // محاسبه قیمت کل برای این سبد خاص
                    BigDecimal totalPrice = cart.getItems().stream() // روی آیتم های این سبد پیمایش کن
                            // اول مطمئن شو که آیتم، کتاب و قیمت کتاب null نیستن
                            .filter(item -> item.getBook() != null && item.getBook().getPrice() != null)
                            // قیمت هر آیتم = قیمت کتاب * تعدادش
                            .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            // همه قیمت های آیتم ها رو با هم جمع بزن (مقدار اولیه جمع صفر است)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // حالا CartSummaryDTO رو با اطلاعات سبد و قیمت محاسبه شده بساز
                    return CartSummaryDTO.builder()
                            .id(cart.getId())
                            .createdAt(cart.getCreatedAt())
                            .status(cart.getStatus())
                            .totalPrice(totalPrice) // قیمت محاسبه شده در اینجا قرار میگیره
                            .build();
                })
                .collect(Collectors.toList());

        // --- ۵. شمارش تعداد کتاب های کاربر با کوئری بهینه ---
        int bookCount = bookRepository.countByOwnerId(userId); // فراخوانی متد جدید در BookRepository

        // --- ۶. ساختن UserResponse نهایی با تمام اطلاعات ---
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .numberOfOwnedBooks(bookCount) // تعداد کتاب های محاسبه شده
                .cartSummaries(cartSummaries) // لیست خلاصه سبدهای خرید محاسبه شده
                .build();
    }

}
