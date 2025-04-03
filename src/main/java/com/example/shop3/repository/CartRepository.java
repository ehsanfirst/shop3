package com.example.shop3.repository;

import com.example.shop3.model.Cart;
import com.example.shop3.model.CartStatus;
import org.springframework.data.domain.Page; // <-- Import Page
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // کوئری برای گرفتن Cart ها به همراه جزئیات برای یک صفحه خاص
    @Query(value = "SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.book b WHERE c.user.id = :userId ORDER BY c.createdAt DESC", // اضافه کردن ORDER BY برای سازگاری با Pageable
            countQuery = "SELECT count(c) FROM Cart c WHERE c.user.id = :userId") // کوئری جداگانه برای شمارش کل
    Page<Cart> findCartsWithDetailsByUserId(@Param("userId") Long userId, Pageable pageable); // <-- تغییر خروجی به Page<Cart>

    // متد countQuery در کوئری بالا کار شمارش را انجام می‌دهد.
    // نیازی به متد countByUserId جداگانه نیست، مگر اینکه از countQuery استفاده نکنید.
    // long countByUserId(Long userId);

    Optional<Cart> findCartByStatusAndUser_Id(CartStatus status, Long userId);
}