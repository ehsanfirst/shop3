package com.example.shop3.dto;

import com.example.shop3.model.Item;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
public class ActiveCartDTO {

    private BigDecimal price;
    private List<Item> items;
}
