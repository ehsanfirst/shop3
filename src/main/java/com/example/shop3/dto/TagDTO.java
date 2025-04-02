package com.example.shop3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TagDTO {
    private Long id;
    private String name;
    private String slug;
}
