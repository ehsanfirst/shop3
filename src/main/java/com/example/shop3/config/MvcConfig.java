package com.example.shop3.config; // مطمئن شو پکیج درسته

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration; // <-- مهمه!
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration // <-- این انوتیشن باعث می‌شه Spring این کلاس رو پیدا کنه
public class MvcConfig implements WebMvcConfigurer {

    // مسیر آپلود رو از application.properties می‌خونه
    @Value("${app.upload-dir}")
    private String uploadDir;

    // الگوی URL که می‌خوایم مدیریت کنیم
    private static final String AVATAR_URL_PATTERN = "/avatars/**";

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
    }
}