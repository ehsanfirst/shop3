package com.example.shop3.dto;

import com.example.shop3.model.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Builder
public class BookResponse {

    private Long id;
    private String name;
    private OwnerDTO owner;
    private String cover;
    private Integer number;
    private Set<TagDTO> tags;
    private BigDecimal price;

}
