package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"}), // نام تگ باید منحصر به فرد باشد
        @UniqueConstraint(columnNames = {"slug"})  // اسلاگ هم باید منحصر به فرد باشد
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "books") // برای جلوگیری از حلقه در رابطه دوطرفه
@EqualsAndHashCode(exclude = "books") // بر اساس id یا name/slug مقایسه شود، نه لیست کتاب‌ها
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // طول نام تگ را محدود می‌کنیم
    private String name;

    @Column(nullable = false, length = 100) // نام مناسب برای URL
    private String slug; // مثال: "dastani", "porforoush-ha"

    // --- رابطه چند-به-چند با Book (سمت معکوس) ---
    // مدیریت این رابطه توسط فیلد 'tags' در کلاس Book انجام می‌شود.
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    // سازنده سفارشی برای راحتی (بدون id و books)
    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}