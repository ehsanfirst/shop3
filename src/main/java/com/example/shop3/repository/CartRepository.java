package com.example.shop3.repository;

import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.model.Cart;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Long> {

//     @Query("SELECT new com.example.shop3.dto.CartSummaryDTO(c.id, c.createdAt, c.status, " +
//             // Cast کردن کل نتیجه COALESCE به BigDecimal
//             " CAST( COALESCE(SUM( CAST(i.quantity AS java.math.BigDecimal) * b.price ), 0.0) AS java.math.BigDecimal) )" +
//             "FROM Cart c LEFT JOIN c.items i LEFT JOIN i.book b " +
//             "WHERE c.user.id = :userId " +
//             "GROUP BY c.id, c.createdAt, c.status " +
//             "ORDER BY c.createdAt DESC")
//     List<CartSummaryDTO> findCartSummariesByUserId(@Param("userId") Long userId, Pageable pageable);

//     // کوئری جدید برای واکشی Cart ها به همراه Item و Book با یک کوئری
//     // DISTINCT برای جلوگیری از کارتیزی شدن نتیجه بخاطر join ها
//     @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.book b WHERE c.user.id = :userId")
//     List<Cart> findCartsWithDetailsByUserId(@Param("userId") Long userId, Pageable pageable); // Pageable همچنان برای محدودیت 5 تا

//     @Query("SELECT new com.example.shop3.dto.CartSummaryDTO(c.id, c.createdAt, c.status, " +
//             // Cast کردن کل نتیجه SUM (و COALESCE)
//             " CAST( COALESCE(SUM( CAST(i.quantity AS java.math.BigDecimal) * b.price ), 0.0) AS java.math.BigDecimal) )" +
//             "FROM Cart c LEFT JOIN c.items i LEFT JOIN i.book b " +
//             "WHERE c.user.id = :userId " +
//             "GROUP BY c.id, c.createdAt, c.status " +
//             "ORDER BY c.createdAt DESC")
//     List<CartSummaryDTO> findCartSummariesByUserId(@Param("userId") Long userId, Pageable pageable);



    // کوئری جدید برای گرفتن Cart ها به همراه Item ها و Book های داخل Item ها
    // با یک دستور بهینه
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.book b WHERE c.user.id = :userId")
    List<Cart> findCartsWithDetailsByUserId(@Param("userId") Long userId, Pageable pageable);
    // Pageable همچنان برای محدودیت 5 تا و مرتب سازی استفاده می شود
}

