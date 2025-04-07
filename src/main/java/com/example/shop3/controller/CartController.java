package com.example.shop3.controller;

import com.example.shop3.config.SecurityUserDetails;
import com.example.shop3.dto.ActiveCartDTO;
import com.example.shop3.dto.AddItemRequest;
import com.example.shop3.dto.CartSummaryDTO;
import com.example.shop3.exception.BookNotFoundException;
import com.example.shop3.exception.ItemNotFoundException;
import com.example.shop3.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;            // <-- Import Page
import org.springframework.data.domain.Pageable;        // <-- Import Pageable
import org.springframework.data.web.PageableDefault;   // <-- Import PageableDefault
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

// import java.util.List; // دیگر نیازی به List نیست

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public String showAllCarts( // اسم متد رو واضح‌تر کردم
                                @AuthenticationPrincipal SecurityUserDetails currentUser,
                                // Pageable رو به عنوان پارامتر اضافه کن
                                // PageableDefault اندازه پیش‌فرض صفحه رو 10 و مرتب‌سازی پیش‌فرض رو تعیین می‌کنه
                                @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
                                Model model) {

        Long userId = currentUser.getUserEntity().getId();

        // فراخوانی سرویس با userId و pageable
        Page<CartSummaryDTO> cartPage = cartService.getCartsByUserId(userId, pageable);

        // اضافه کردن Page به Model به جای List
        model.addAttribute("cartPage", cartPage); // <-- نام attribute رو هم به cartPage تغییر دادم

        // اضافه کردن اطلاعات کاربر (اگر در صفحه لازم دارید)
        // model.addAttribute("currentUser", currentUser.getUserEntity());

        return "carts/showCarts"; // نام تمپلیت شما
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public String activeCart(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            Model model
    ){
        ActiveCartDTO activeCartDTO=cartService.getActiveCartByUserId(userDetails.getUserEntity().getId());
        model.addAttribute("cart",activeCartDTO);
        return "carts/activeCart";
    }


    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody // <--- اضافه شد: برای برگرداندن پاسخ در بدنه، نه view/redirect
    public ResponseEntity<?> handleAddToCartAjax( // نام متد را برای وضوح تغییر دادم
                                                  @Valid @ModelAttribute AddItemRequest addItemRequest, // همچنان از DTO استفاده می‌کنیم
                                                  BindingResult bindingResult,
                                                  @AuthenticationPrincipal SecurityUserDetails currentUser) { // RedirectAttributes دیگر لازم نیست

        // بررسی خطاهای اعتبارسنجی DTO
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            // برگرداندن پاسخ خطا با وضعیت 400
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "خطا در اطلاعات ورودی: " + errorMsg));
        }

        try {
            Long userId = currentUser.getUserEntity().getId();
            cartService.addItemToCart(userId, addItemRequest.getBookId(), addItemRequest.getQuantity());

            // برگرداندن پاسخ موفقیت با وضعیت 200 OK
            return ResponseEntity.ok(Map.of("success", true, "message", "کتاب با موفقیت به سبد خرید اضافه شد."));

        } catch (BookNotFoundException e) {
            // برگرداندن پاسخ خطا با وضعیت 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "خطا: کتاب مورد نظر یافت نشد."));
        } catch (IllegalStateException e) { // برای خطای موجودی
            // برگرداندن پاسخ خطا با وضعیت 400 Bad Request
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "خطا: " + e.getMessage()));
        } catch (Exception e) {
            // برگرداندن پاسخ خطای عمومی با وضعیت 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "خطا در افزودن به سبد خرید: " + e.getMessage()));
        }

    }

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody // <--- اضافه شد: برای برگرداندن پاسخ در بدنه، نه view/redirect
    public ResponseEntity<?> handleUpdateCartItem( // نام متد را برای وضوح تغییر دادم
                                                  @Valid @ModelAttribute AddItemRequest addItemRequest, // همچنان از DTO استفاده می‌کنیم
                                                  BindingResult bindingResult,
                                                  @AuthenticationPrincipal SecurityUserDetails currentUser) { // RedirectAttributes دیگر لازم نیست

        // بررسی خطاهای اعتبارسنجی DTO
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            // برگرداندن پاسخ خطا با وضعیت 400
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "خطا در اطلاعات ورودی: " + errorMsg));
        }

        try {
            Long userId = currentUser.getUserEntity().getId();
            cartService.updateItemQuantity(userId, addItemRequest.getBookId(), addItemRequest.getQuantity());

            // برگرداندن پاسخ موفقیت با وضعیت 200 OK
            return ResponseEntity.ok(Map.of("success", true, "message", "تعداد کتاب با موفقیت بروز رسانی شد."));

        } catch (ItemNotFoundException e) {
            // برگرداندن پاسخ خطا با وضعیت 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "خطا: آیتم مورد نظر یافت نشد."));
        } catch (IllegalStateException e) { // برای خطای موجودی
            // برگرداندن پاسخ خطا با وضعیت 400 Bad Request
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "خطا: " + e.getMessage()));
        } catch (Exception e) {
            // برگرداندن پاسخ خطای عمومی با وضعیت 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "خطا در افزودن به سبد خرید: " + e.getMessage()));
        }

    }




}