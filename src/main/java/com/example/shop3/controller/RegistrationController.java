package com.example.shop3.controller;

import com.example.shop3.dto.UserRequest;
import com.example.shop3.model.User;
import com.example.shop3.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    // --- تزریق مسیر آپلود از application.properties ---
    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final String AVATAR_URL_PREFIX = "/avatars/";

    // متد برای ایجاد پوشه آپلود در صورت عدم وجود (فراخوانی در متد POST)
    private Path ensureUploadDirectoryExists()throws IOException {
        Path path = Paths.get(uploadDir);
        if(!Files.exists(path)){
            Files.createDirectories(path);
            System.out.println("the path is created by url: "  + uploadDir);
        }
        return path;
    }

    //به صفحه بفهمونیم چه فیلدایی داره
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user",new UserRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("user") UserRequest user, // یا DTO
            BindingResult bindingResult,
            @RequestParam("avatarFile") MultipartFile avatarFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        Path uploadPath;
        try {
            // اطمینان از وجود پوشه آپلود
            uploadPath = ensureUploadDirectoryExists();
        } catch (IOException e) {
            System.err.println("FATAL: Could not create upload directory: " + uploadDir + " - " + e.getMessage());
            model.addAttribute("globalError", "خطای داخلی سرور هنگام آماده‌سازی آپلود فایل.");
            return "register";
        }

        Path savedFilePath = null;
        String relativeAvatarUrl = null; // URL نسبی برای ذخیره در دیتابیس

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) { // بررسی کلی تر برای انواع عکس
                model.addAttribute("fileError", "فرمت فایل نامعتبر است. لطفاً یک فایل تصویری آپلود کنید.");
                return "register";
            }

            // --- ساخت نام فایل امن با UUID ---
            String originalFilename = avatarFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // بررسی پسوندهای مجاز (مثال)
            if (!fileExtension.equalsIgnoreCase(".jpg") && !fileExtension.equalsIgnoreCase(".jpeg") && !fileExtension.equalsIgnoreCase(".png") && !fileExtension.equalsIgnoreCase(".gif")){
                model.addAttribute("fileError", "فرمت فایل تصویر مجاز نیست (فقط jpg, png, gif).");
                return "register";
            }

            String filename = UUID.randomUUID().toString() + fileExtension; // نام فایل منحصر به فرد
            Path destinationFile = uploadPath.resolve(filename).normalize();

            try (InputStream inputStream = avatarFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                savedFilePath = destinationFile;
                // --- ساخت URL نسبی ---
                relativeAvatarUrl = AVATAR_URL_PREFIX + filename; // مثل /avatars/uuid-random.jpg

                System.out.println("File saved successfully to: " + savedFilePath);
                System.out.println("Relative URL for DB/HTML: " + relativeAvatarUrl);

            } catch (IOException e) {
                System.err.println("Could not save file: " + filename + " - " + e.getMessage());
                model.addAttribute("fileError", "خطا در ذخیره سازی فایل آواتار.");
                return "register";
            }
        } else {
            System.out.println("No avatar file provided or file is empty.");
            // می توانید آواتار پیش فرض ست کنید
            // relativeAvatarUrl = "/images/avatar/default-avatar.png"; // آواتار پیش فرض از static
        }

        try {
            // فقط DTO و URL آواتار را به سرویس پاس می‌دهیم
            userService.registerUser(user, relativeAvatarUrl); // نیاز به تغییر متد سرویس

            redirectAttributes.addFlashAttribute("successMessage", "ثبت نام شما با موفقیت انجام شد!");
            return "redirect:/login";


        } catch (Exception e) {
            System.err.println("Registration failed for user: " + user.getUsername() + " - " + e.getMessage());

            if (savedFilePath != null) {
                try {
                    Files.deleteIfExists(savedFilePath);
                    System.out.println("Deleted avatar file due to registration failure: " + savedFilePath);
                } catch (IOException cleanupEx) {
                    System.err.println("Error deleting avatar file after registration failure: " + cleanupEx.getMessage());
                }
            }

            // TODO: تبدیل Exception به پیام‌های خطای مناسب‌تر برای کاربر
            if (e.getMessage().contains("ConstraintViolationException") /* بررسی دقیق تر لازم است */) {
                if (e.getMessage().contains("users_username_key") || e.getMessage().contains("USERNAME")) { // نام constraint ممکن است متفاوت باشد
                    bindingResult.rejectValue("username", "error.user", "این نام کاربری قبلا ثبت شده است.");
                } else if (e.getMessage().contains("users_email_key")|| e.getMessage().contains("EMAIL")) {
                    bindingResult.rejectValue("email", "error.user", "این ایمیل قبلا ثبت شده است.");
                } else {
                    model.addAttribute("registrationError", "خطا در ثبت نام. لطفا اطلاعات را بررسی کنید.");
                }
            } else {
                model.addAttribute("registrationError", "خطا در ثبت نام: " + e.getMessage());
            }
            return "register";
        }
    }



}//class
