package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item") // نام جدول در دیتابیس
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// در toString و equals/hashCode از نمایش/مقایسه book و cart صرف نظر می‌کنیم
// تا از حلقه‌های احتمالی در روابط دوطرفه جلوگیری شود.
@ToString(exclude = {"book", "cart"})
@EqualsAndHashCode(exclude = {"book", "cart"})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- رابطه چند-به-یک با Book ---
    // هر آیتم سبد خرید به یک کتاب خاص اشاره دارد
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // هر آیتم *باید* به یک کتاب مرتبط باشد (optional=false)
    @JoinColumn(name = "book_id", nullable = false) // نام ستون کلید خارجی در جدول item
    private Book book;

    // --- رابطه چند-به-یک با Cart ---
    // هر آیتم سبد خرید به یک سبد خرید خاص تعلق دارد
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // هر آیتم *باید* به یک سبد خرید مرتبط باشد
    @JoinColumn(name = "cart_id", nullable = false) // نام ستون کلید خارجی در جدول item
    private Cart cart;

    @Column(nullable = false) // تعداد نمی‌تواند null باشد
    private int quantity; // تعداد این کتاب در سبد خرید

    // --- سازنده سفارشی (اختیاری) ---
    // ممکن است برای ایجاد راحت‌تر آیتم‌ها مفید باشد
    public Item(Book book, Cart cart, int quantity) {
        this.book = book;
        this.cart = cart;
        this.quantity = quantity;
    }

    // --- متدهای کمکی (اختیاری) ---
    // برای محاسبه قیمت کل این آیتم (تعداد * قیمت واحد کتاب)
    // توجه: این متد مستقیماً به دیتابیس مرتبط نیست ولی می‌تواند در لایه سرویس یا حتی در Entity مفید باشد
    // برای استفاده از این متد، fetch = FetchType.LAZY برای book باید مدیریت شود (یا EAGER باشد که توصیه نمی‌شود)
    /*
    public BigDecimal getTotalPrice() {
        if (book != null && book.getPrice() != null) {
            return book.getPrice().multiply(BigDecimal.valueOf(this.quantity));
        }
        return BigDecimal.ZERO;
    }
    */
}