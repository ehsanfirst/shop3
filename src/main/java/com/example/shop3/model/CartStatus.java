package com.example.shop3.model;
// Enum برای وضعیت سبد خرید (اختیاری ولی توصیه می‌شود)
public enum CartStatus {
    ACTIVE,    // سبد خرید فعال و در حال استفاده
    ORDERED,   // سبد خرید به سفارش تبدیل شده
    ABANDONED  // سبد خرید رها شده (مثلا پس از مدتی عدم فعالیت)
}
