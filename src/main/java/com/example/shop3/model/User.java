package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") // اگر نام جدول در دیتابیس متفاوت است
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String avatar; // اختیاری

    @CreationTimestamp
    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp

    private LocalDateTime updatedAt;

    private boolean enabled = true; // پیشفرض فعال است

    @Enumerated(EnumType.STRING) // نقش را به صورت متن در دیتابیس ذخیره میکنیم
    private Role role = Role.USER; // پیشفرض نقش کاربر عادی
}