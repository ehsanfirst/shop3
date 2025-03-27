package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.*; // اضافه کردن @ToString و @EqualsAndHashCode
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// import org.springframework.data.annotation.CreatedDate; // اگر از Auditing استفاده می‌کنید، یکی را انتخاب کنید

import java.time.LocalDateTime;
import java.util.ArrayList; // اضافه شد
import java.util.List;     // اضافه شد

@Entity
@Data // شامل @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
// @ToString و @EqualsAndHashCode را از @Data جدا می‌کنیم تا بتوانیم exclude کنیم
@ToString(exclude = {"ownedBooks", "cart"}) // از نمایش کتاب‌ها و سبد خرید در toString جلوگیری می‌کنیم
@EqualsAndHashCode(exclude = {"ownedBooks", "cart"}) // از کتاب‌ها و سبد خرید در مقایسه صرف نظر می‌کنیم
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

    private String avatar;

    @CreationTimestamp
    // @CreatedDate // اگر از Auditing استفاده می‌کنید یکی کافی است
    @Column(name = "created_at", nullable = false, updatable = false) // صریحاً نام ستون را مشخص کنیم
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false) // صریحاً نام ستون را مشخص کنیم
    private LocalDateTime updatedAt;

    @Column(name = "enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // --- شروع بخش اضافه شده برای رابطه دوطرفه با Book ---
    @OneToMany(
            mappedBy = "owner", // نام فیلد 'owner' در کلاس Book که این رابطه را مدیریت می‌کند
            cascade = CascadeType.PERSIST, // یا سایر انواع Cascade، با احتیاط انتخاب شود
            fetch = FetchType.LAZY // **بسیار مهم** برای جلوگیری از لود شدن تمام کتاب‌ها به صورت پیش‌فرض
    )
    private List<Book> ownedBooks = new ArrayList<>();
    // --- پایان بخش اضافه شده ---


    // --- رابطه یک-به-چند با Cart ---
    @OneToMany(
            mappedBy = "user", // نام فیلد 'user' در کلاس Cart
            cascade = CascadeType.ALL, // با احتیاط استفاده شود (مخصوصا REMOVE)
            fetch = FetchType.LAZY,
            orphanRemoval = true // اگر می‌خواهید با حذف Cart از این لیست، از دیتابیس هم حذف شود
    )
    private List<Cart> carts = new ArrayList<>(); // تغییر به List و نام جمع + مقداردهی اولیه

    // --- به‌روزرسانی exclude ها در بالای کلاس ---
    // @ToString(exclude = {"ownedBooks", "carts"}) // نام به carts تغییر کرد
    // @EqualsAndHashCode(exclude = {"ownedBooks", "carts"}) // نام به carts تغییر کرد


    // --- متدهای کمکی برای مدیریت رابطه دوطرفه (اختیاری ولی خوب است) ---
    public void addBook(Book book) {
        this.ownedBooks.add(book);
        book.setOwner(this);
    }

    public void removeBook(Book book) {
        this.ownedBooks.remove(book);
        book.setOwner(null);
    }

}