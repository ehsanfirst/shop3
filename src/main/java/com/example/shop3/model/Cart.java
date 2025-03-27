package com.example.shop3.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // یا از Spring Data JPA Auditing

import java.math.BigDecimal; // برای محاسبه قیمت کل
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Enum برای وضعیت سبد خرید (اختیاری ولی توصیه می‌شود)
enum CartStatus {
    ACTIVE,    // سبد خرید فعال و در حال استفاده
    ORDERED,   // سبد خرید به سفارش تبدیل شده
    ABANDONED  // سبد خرید رها شده (مثلا پس از مدتی عدم فعالیت)
}

@Entity
@Table(name = "cart") // نام جدول در دیتابیس
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// از نمایش/مقایسه user و items در toString و equals/hashCode صرف نظر می‌کنیم
@ToString(exclude = {"user", "items"})
@EqualsAndHashCode(exclude = {"user", "items"})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- رابطه یک-به-یک با User ---
    // هر سبد خرید به یک کاربر تعلق دارد.
    // JoinColumn در این سمت قرار می‌گیرد چون cart جدول حاوی user_id است.
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // هر سبد خرید به یک کاربر تعلق دارد (Many To One)
    @JoinColumn(name = "user_id", nullable = false) // نام ستون کلید خارجی در جدول cart و غیرقابل null بودن
    private User user;

    @CreationTimestamp // یا @CreatedDate با Auditing
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // وضعیت را به صورت متن ذخیره کن (ACTIVE, ORDERED, ...)
    @Column(nullable = false)
    private CartStatus status = CartStatus.ACTIVE; // وضعیت پیش‌فرض

    // --- رابطه یک-به-چند با Item ---
    // این سمتِ معکوسِ رابطه است. مدیریت توسط فیلد 'cart' در کلاس Item انجام می‌شود.
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL, // با حذف سبد، آیتم‌هایش هم حذف شوند (معمولاً منطقی است)
            orphanRemoval = true,    // با حذف آیتم از لیست، از دیتابیس هم حذف شود
            fetch = FetchType.LAZY     // آیتم‌ها فقط در صورت نیاز لود شوند
    )
    private List<Item> items = new ArrayList<>();

    // --- متدهای کمکی (اختیاری ولی مفید) ---

    // اضافه کردن آیتم به سبد و مدیریت رابطه دوطرفه
    public void addItem(Item item) {
        // جلوگیری از افزودن آیتم‌های null
        if (item != null) {
            // بررسی اینکه آیا کتاب این آیتم قبلاً در سبد هست؟
            for (Item existingItem : this.items) {
                if (existingItem.getBook().equals(item.getBook())) {
                    // اگر هست، فقط تعداد را اضافه کن
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    // نیازی به تنظیم cart برای item نیست چون از قبل وجود داشته
                    return; // از متد خارج شو
                }
            }
            // اگر کتاب در سبد نبود، آیتم جدید را اضافه کن
            this.items.add(item);
            item.setCart(this);
        }
    }


    // حذف آیتم از سبد و مدیریت رابطه دوطرفه
    public void removeItem(Item item) {
        if (item != null) {
            boolean removed = this.items.remove(item);
            if (removed) {
                item.setCart(null); // قطع ارتباط آیتم با این سبد
            }
        }
    }

    // حذف آیتم بر اساس ID کتاب (مثال دیگر)
    public void removeItemByBookId(Long bookId) {
        Item itemToRemove = null;
        for (Item item : this.items) {
            if (item.getBook() != null && item.getBook().getId().equals(bookId)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            removeItem(itemToRemove);
        }
    }

    // محاسبه قیمت کل سبد خرید
    // نیاز به مدیریت LazyInitializationException دارد اگر خارج از تراکنش استفاده شود
    /*
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Item item : items) {
            // اطمینان از اینکه قیمت آیتم قابل محاسبه است
            BigDecimal itemTotalPrice = item.getTotalPrice(); // فراخوانی متد کمکی در Item
            if (itemTotalPrice != null) {
                 totalPrice = totalPrice.add(itemTotalPrice);
            }
        }
        return totalPrice;
    }
    */

    // محاسبه تعداد کل آیتم‌ها (نه تعداد کتاب‌ها)
    public int getTotalItemCount() {
        return this.items.stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

}