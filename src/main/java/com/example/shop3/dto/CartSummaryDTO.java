package com.example.shop3.dto; // یا هر پکیج مناسب دیگر

import com.example.shop3.model.CartStatus; // Enum وضعیت سبد خرید
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {

    private Long id; // برای لینک دادن به صفحه جزئیات سبد
    private LocalDateTime createdAt;
    private CartStatus status;
    private BigDecimal totalPrice; // قیمت کل محاسبه شده
    // می‌تونی تعداد آیتم‌ها رو هم اضافه کنی اگه لازمه:
    // private int totalItemCount;




}