package com.example.shop3.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OwnerDTO {
    String username;
    String email;
    String avatar;

}
