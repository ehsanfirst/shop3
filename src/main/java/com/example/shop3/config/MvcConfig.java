package com.example.shop3.config; // مطمئن شو پکیج درسته

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration // <-- این انوتیشن باعث می‌شه Spring این کلاس رو پیدا کنه
public class MvcConfig implements WebMvcConfigurer {

    // مسیر آپلود رو از application.properties می‌خونه
    @Value("${app.upload-dir}")
    private String uploadDir;
    private static final String AVATAR_URL_PATTERN = "/avatars/**";    // الگوی URL که می‌خوایم مدیریت کنیم

    @Value("${app.upload.cover-dir}")
    private String coverUploadDir;
    private static final String COVER_URL_PATTERN = "/covers/**"; // الگوی URL برای کاورها


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // مسیر فیزیکی رو پیدا می‌کنه
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        // تبدیلش می‌کنه به فرمت URI که Resource Handler می‌فهمه (مثل file:/C:/...)
        String uploadPathUri = uploadPath.toUri().toString();

        // اینجا می‌تونی مسیرها رو چاپ کنی تا مطمئن شی درستن (اختیاری برای دیباگ)
        System.out.println("Registering resource handler: " + AVATAR_URL_PATTERN + " -> " + uploadPathUri);

        // ثبت کردن راهنما: درخواست‌های با الگوی AVATAR_URL_PATTERN رو بفرست به مسیر uploadPathUri
        registry.addResourceHandler(AVATAR_URL_PATTERN)
                .addResourceLocations(uploadPathUri);

        // --- ثبت Handler جدید برای کاور کتاب ها ---
        Path coverPath = Paths.get(coverUploadDir).toAbsolutePath();
        String coverPathUri = coverPath.toUri().toString();
        System.out.println("Registering resource handler: " + COVER_URL_PATTERN + " -> " + coverPathUri);
        registry.addResourceHandler(COVER_URL_PATTERN) // استفاده از الگوی URL کاور
                .addResourceLocations(coverPathUri);     // استفاده از مسیر فیزیکی کاور
    }


}