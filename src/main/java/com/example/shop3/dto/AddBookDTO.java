package com.example.shop3.dto;


import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddBookDTO {

    @NotBlank(message = "نام کتاب نمی تواند خالی باشد")
    @Size(message = "نام گتاب حداکثر 200 کاراکتر باید باشد")
    private String name;

    @NotNull(message = "تعداد موجودی را مشخص کنید")
    @Min(value = 1, message = "تعداد موجودی بیشتر از 0 باید باشد")
    private int availableNum;

    @NotNull(message ="قیمت را مشخص کن" )
    @DecimalMin(value = "0.0" , inclusive = false , message = "قیمت باید بزرگتر از صفحر باشد")
    @Digits(integer=8, fraction=2, message = "قیمت نامعتبر است (حداکثر ۸ رقم صحیح و ۲ رقم اعشار)")
    private BigDecimal price;

    // فیلد برای دریافت ID تگ های انتخاب شده از فرم
    private List<Long> tagIds = new ArrayList<>();



}
