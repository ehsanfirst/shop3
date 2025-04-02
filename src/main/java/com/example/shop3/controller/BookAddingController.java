package com.example.shop3.controller;

import com.example.shop3.config.SecurityUserDetails; // اضافه شد
import com.example.shop3.dto.AddBookDTO;
import com.example.shop3.model.Book;
import com.example.shop3.model.Tag; // اضافه شد
import com.example.shop3.service.BookService;
import com.example.shop3.service.TagService; // اضافه شد (یا مستقیم از TagRepository)
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // اضافه شد
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*; // RequestMapping اضافه شد
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List; // اضافه شد
import java.util.UUID;

@Controller
@RequestMapping("/books") // بهتره URL های مربوط به کتاب زیرمجموعه /books باشن
@RequiredArgsConstructor
public class BookAddingController {

    private final BookService bookService;
    private final TagService tagService; // برای گرفتن لیست تگ ها

    @Value("${app.upload.cover-dir}")
    private String coverUploadDir;
    // این ثابت دیگه مستقیم استفاده نمیشه برای ساخت URL
    // private static final String coverURL = "/covers/**";
    private static final String COVER_URL_PREFIX = "/covers/"; // <- از این برای ساخت URL استفاده می کنیم

    // --- نمایش فرم افزودن کتاب ---
    @GetMapping("/new")
    public String showAddBookForm(Model model) {
        // 1. اضافه کردن DTO خالی برای فرم
        model.addAttribute("book", new AddBookDTO());
        // 2. گرفتن و اضافه کردن لیست تمام تگ ها
        List<Tag> allTags = tagService.getAllTags(); // فرض: متدی در TagService برای گرفتن همه تگ ها
        model.addAttribute("allTags", allTags);
        // 3. بازگرداندن نام فایل HTML فرم
        return "books/add-book-form"; // <- نام جدید و در پوشه books
    }

    // --- پردازش فرم افزودن کتاب ---
    @PostMapping("/new")
    public String processAddBook(
            @Valid @ModelAttribute("book") AddBookDTO bookDTO, // اسم رو به bookDTO تغییر دادم که با book entity اشتباه نشه
            BindingResult bindingResult, // BindingResult باید بلافاصله بعد از پارامتر @Valid بیاد
            @RequestParam("coverFile") MultipartFile coverFile, // اسم پارامتر باید با name در input فایل یکی باشه
            @AuthenticationPrincipal SecurityUserDetails currentUser, // گرفتن کاربر لاگین کرده
            RedirectAttributes redirectAttributes,
            Model model) {

        // اگر خطای اعتبارسنجی در DTO وجود داشت
        if (bindingResult.hasErrors()) {
            // لیست تگ ها رو دوباره به model اضافه کن چون صفحه دوباره لود میشه
            List<Tag> allTags = tagService.getAllTags();
            model.addAttribute("allTags", allTags);
            return "books/add-book-form"; // نمایش دوباره فرم با خطاها
        }

        // بررسی وجود کاربر (هرچند Spring Security باید هندل کنه، ولی محض اطمینان)
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "لطفا ابتدا وارد شوید.");
            return "redirect:/login";
        }

        Path uploadPath;
        try {
            uploadPath = ensureUploadDirectoryExists();
        } catch (IOException e) {
            System.err.println("FATAL: Could not create upload directory: " + coverUploadDir + " - " + e.getMessage());
            model.addAttribute("globalError", "خطای داخلی سرور هنگام آماده‌سازی آپلود فایل.");
            // تگ ها رو دوباره لود کن
            List<Tag> allTags = tagService.getAllTags();
            model.addAttribute("allTags", allTags);
            return "books/add-book-form";
        }

        Path savedFilePath = null;
        String relativeCoverUrl = null;

        // --- پردازش فایل آپلود شده ---
        if (coverFile != null && !coverFile.isEmpty()) {
            // (اعتبارسنجی نوع و پسوند فایل مثل قبل - کد شما خوب بود)
            String contentType = coverFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("fileError", "فقط فایل تصویری مجاز است.");
                List<Tag> allTags = tagService.getAllTags(); model.addAttribute("allTags", allTags); // تگ‌ها فراموش نشه
                return "books/add-book-form";
            }
            // میتونی محدودیت حجم هم اضافه کنی
            // if (coverFile.getSize() > MAX_FILE_SIZE) { ... }

            String originalFilename = coverFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // (اعتبارسنجی پسوند مثل قبل)
            if (!fileExtension.matches("(?i)\\.(jpg|jpeg|png|gif)$")) { // استفاده از regex برای سادگی
                model.addAttribute("fileError", "فرمت فایل تصویر مجاز نیست (فقط jpg, png, gif).");
                List<Tag> allTags = tagService.getAllTags(); model.addAttribute("allTags", allTags); // تگ‌ها فراموش نشه
                return "books/add-book-form";
            }


            String filename = UUID.randomUUID().toString() + fileExtension;
            Path destinationFile = uploadPath.resolve(filename).normalize();

            try (InputStream inputStream = coverFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                savedFilePath = destinationFile;
                // --- ساخت URL نسبی صحیح ---
                relativeCoverUrl = COVER_URL_PREFIX + filename; // مثل /covers/uuid-random.jpg <- اصلاح شد

                System.out.println("Cover file saved successfully to: " + savedFilePath);
                System.out.println("Relative URL for DB/HTML: " + relativeCoverUrl);

            } catch (IOException e) {
                System.err.println("Could not save cover file: " + filename + " - " + e.getMessage());
                model.addAttribute("fileError", "خطا در ذخیره سازی فایل کاور.");
                List<Tag> allTags = tagService.getAllTags(); model.addAttribute("allTags", allTags); // تگ‌ها فراموش نشه
                return "books/add-book-form";
            }
        } else {
            System.out.println("No cover file provided or file is empty.");
            // اینجا می تونی URL یک کاور پیش فرض رو ست کنی اگه خواستی
            // relativeCoverUrl = "/images/covers/default-cover.png";
        }

        // --- فراخوانی سرویس برای ذخیره کتاب ---
        try {
            // سرویس باید کاربر رو هم بگیره و ID کتاب ذخیره شده رو برگردونه
            Book savedBook = bookService.registerBook(bookDTO, relativeCoverUrl, currentUser.getUserEntity()); // <- پاس دادن کاربر

            redirectAttributes.addFlashAttribute("successMessage", "کتاب \"" + savedBook.getName() + "\" با موفقیت افزوده شد!");
            // ریدایرکت به صفحه خود کتاب (مثلا)
            return "redirect:/books/" + savedBook.getId(); // <- ریدایرکت به جزئیات کتاب

        } catch (Exception e) { // بهتره Exception های خاص تری رو catch کنی
            System.err.println("Book registration failed for: " + bookDTO.getName() + " - " + e.getMessage());

            // اگر فایل ذخیره شده بود ولی ثبت ناموفق بود، فایل رو پاک کن
            if (savedFilePath != null) {
                try {
                    Files.deleteIfExists(savedFilePath);
                    System.out.println("Deleted cover file due to registration failure: " + savedFilePath);
                } catch (IOException cleanupEx) {
                    System.err.println("Error deleting cover file after registration failure: " + cleanupEx.getMessage());
                }
            }

            // نمایش خطای عمومی به کاربر
            model.addAttribute("globalError", "خطا در افزودن کتاب: " + e.getMessage()); // پیام خطا رو به کاربر نشون بده
            List<Tag> allTags = tagService.getAllTags(); model.addAttribute("allTags", allTags); // تگ‌ها فراموش نشه
            return "books/add-book-form"; // بازگشت به فرم
        }
    }


    // متد کمکی برای ایجاد پوشه آپلود (کد شما خوب بود)
    private Path ensureUploadDirectoryExists() throws IOException {
        Path path = Paths.get(coverUploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("Upload directory created: " + coverUploadDir);
        }
        return path;
    }
}