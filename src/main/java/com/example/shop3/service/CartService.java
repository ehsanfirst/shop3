package com.example.shop3.service;

import com.example.shop3.dto.ActiveCartDTO;
import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.exception.BookNotFoundException;
import com.example.shop3.exception.ItemNotFoundException;
import com.example.shop3.model.*;
import com.example.shop3.repository.BookRepository;
import com.example.shop3.repository.CartRepository;
import com.example.shop3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;        // <-- Import Page
import org.springframework.data.domain.Pageable;    // <-- Import Pageable
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- اضافه شد

import java.math.BigDecimal;
import java.util.ArrayList;
// import java.util.List; // دیگر نیازی به این نیست
// import java.util.stream.Collectors; // برای map کردن Page استفاده می‌شود

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true) // <-- تراکنش فقط خواندنی برای بهینگی
    public Page<CartSummaryDTO> getCartsByUserId(Long userId, Pageable pageable) { // <-- ورودی Pageable و خروجی Page

        // 1. گرفتن Page<Cart> از ریپازیتوری
        Page<Cart> cartPage = cartRepository.findCartsWithDetailsByUserId(userId, pageable);

        // 2. تبدیل Page<Cart> به Page<CartSummaryDTO>
        // متد map خود Page این کار را برای آیتم‌های صفحه فعلی انجام می‌دهد
        return cartPage.map(cart -> { // <-- استفاده از map خود Page
            // محاسبه قیمت کل برای این سبد (کد قبلی شما عالی بود)
            BigDecimal totalPrice = cart.getItems().stream()
                    .filter(item -> item.getBook() != null && item.getBook().getPrice() != null)
                    .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ساخت CartSummaryDTO
            return CartSummaryDTO.builder() // <-- استفاده از بیلدر خواناتر است
                    .id(cart.getId())
                    .createdAt(cart.getCreatedAt())
                    .status(cart.getStatus())
                    .totalPrice(totalPrice)
                    .build();
        });
    }
    @Transactional
    public ActiveCartDTO  getActiveCartByUserId(Long userId) {
        Cart cart =cartRepository.findCartByStatusAndUser_Id(CartStatus.ACTIVE,userId)
                .orElseGet(() -> createActiveCart(userId));
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (cart.getItems()!=null && !cart.getItems().isEmpty()) {
            for(Item item : cart.getItems()) {
                totalPrice = totalPrice.add(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        return ActiveCartDTO.builder().
                items(new ArrayList<>(cart.getItems()))
                .price(totalPrice)
                .build();
    }
    @Transactional
    protected Cart createActiveCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        Cart cart= Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        return cartRepository.save(cart);
    }//createActiveCart

    @Transactional // *** بسیار مهم: عملیات باید اتمی باشد ***
    public void addItemToCart(Long userId, Long bookId, int quantity) { // انوتیشن های اعتبارسنجی حذف شد

        // 1. پیدا کردن یا ساختن سبد فعال کاربر
        Cart cart = cartRepository.findCartByStatusAndUser_Id(CartStatus.ACTIVE, userId)
                .orElseGet(() -> createActiveCart(userId)); // از متد موجود استفاده می‌کنیم

        // 2. پیدا کردن کتاب
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId, bookId)); // پاس دادن ID به Exception

        // 3. بررسی موجودی کتاب *** مهم ***
        if (book.getNumber() == null || book.getNumber() < quantity) {
            // یک Exception مناسب پرتاب کن که کنترلر بتواند آن را گرفته و به کاربر نمایش دهد
            throw new IllegalStateException("Insufficient stock for book: " + book.getName() + ". Available: " + (book.getNumber() != null ? book.getNumber() : 0));
        }

        // 4. ساخت آیتم جدید (فقط با کتاب و تعداد)
        // نیازی به ست کردن cart در اینجا نیست، متد cart.addItem خودش این کار را می‌کند.
        Item newItem = new Item();
        newItem.setBook(book);
        newItem.setQuantity(quantity);

        // 5. استفاده از متد کمکی در Cart برای اضافه کردن/آپدیت کردن آیتم
        // این متد منطق بررسی آیتم موجود و آپدیت تعداد یا افزودن جدید را دارد
        cart.addItem(newItem); // <--- این خط جایگزین itemRepository.save() می شود

        // 6. ذخیره کردن والد (Cart)
        // JPA به دلیل CascadeType.ALL (که روی items در Cart تنظیم شده) خودش Item را هم ذخیره/آپدیت می‌کند.
        cartRepository.save(cart);
    }

    @Transactional
    public void updateItemQuantity(Long userId, Long bookId, int quantity) {
        Cart cart = cartRepository.findCartByStatusAndUser_Id(CartStatus.ACTIVE,userId)
                .orElseThrow(() -> new IllegalStateException("Active cart not found for user: " + userId)); // یا exception مناسب دیگر

        Item item = cart.getItems()
                .stream()
                .filter((item1 -> item1.getBook().getId().equals(bookId)))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ID: " + bookId, bookId));

        Book book = item.getBook();
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity cannot be less than 1.");
            // یا اگر می خوای آیتم رو حذف کنی:
            // cart.removeItem(item); // از متد کمکی استفاده کن
            // cartRepository.save(cart);
            // return; // و از متد خارج شو
        }

        if (book.getNumber()==null||item.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for book: " + book.getName() + ". Available: " + (book.getNumber() != null ? book.getNumber() : 0));
        }

        item.setQuantity(quantity);
        cartRepository.save(cart);
    }


}//class