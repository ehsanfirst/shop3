package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet; // اضافه شد
import java.util.List;
import java.util.Set;    // اضافه شد

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// items و tags را exclude می‌کنیم
@ToString(exclude = {"items", "owner", "tags"})
@EqualsAndHashCode(exclude = {"items", "owner", "tags"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", referencedColumnName = "id")
    private User owner;

    @Column(length = 255)
    private String avatar;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer number; // تعداد موجودی

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    // --- شروع بخش اضافه شده برای رابطه چند-به-چند با Tag ---
    @ManyToMany(fetch = FetchType.LAZY /*, cascade = { CascadeType.PERSIST, CascadeType.MERGE } */ )
    @JoinTable(
            name = "book_tag", // نام جدول اتصال (واسط)
            joinColumns = @JoinColumn(name = "book_id"), // ستون FK اشاره کننده به Book (این کلاس)
            inverseJoinColumns = @JoinColumn(name = "tag_id") // ستون FK اشاره کننده به Tag (طرف دیگر)
    )
    private Set<Tag> tags = new HashSet<>();
    // --- پایان بخش اضافه شده ---


    // --- متدهای کمکی برای مدیریت دوطرفه رابطه Book <-> Item ---
    public void addItem(Item item) {
        items.add(item);
        item.setBook(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setBook(null);
    }

    // --- متدهای کمکی برای مدیریت دوطرفه رابطه Book <-> Tag ---
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getBooks().add(this); // مدیریت سمت معکوس
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getBooks().remove(this); // مدیریت سمت معکوس
    }
}