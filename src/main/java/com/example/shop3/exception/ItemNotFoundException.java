package com.example.shop3.exception; // یا یک پکیج مناسب دیگر مثل common یا error

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// این انوتیشن به Spring میگه که اگر این Exception مدیریت نشه، کد وضعیت 404 رو برگردونه
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ItemNotFoundException extends RuntimeException {

    private final Long itemId; // اختیاری: برای نگه داشتن ID کتابی که پیدا نشده

    public ItemNotFoundException(String message, Long itemId) {
        super(message); // پیام خطا
        this.itemId = itemId;
    }

    public ItemNotFoundException(String message) {
        super(message);
        this.itemId = null; // اگر نخواهیم ID رو نگه داریم
    }

    // Getter اختیاری برای ID
    public Long getBookId() {
        return itemId;
    }
}