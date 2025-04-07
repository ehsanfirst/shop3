package com.example.shop3.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AddItemRequest {

    @NotNull(message = "شناسه کتاب الزامی است.")
    private Long bookId;

    @Min(value = 1, message = "تعداد باید حداقل ۱ باشد.")
    private int quantity = 1; // مقدار پیش‌فرض را می‌توان ۱ گذاشت
}
