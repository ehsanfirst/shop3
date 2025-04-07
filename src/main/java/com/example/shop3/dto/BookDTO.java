package com.example.shop3.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookDTO {

    private Long id;
    private String name;
    private String cover;
    private BigDecimal price;
    private Integer number;
}
