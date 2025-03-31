package com.example.shop3.dto;

import com.example.shop3.model.Cart;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {

    private String username;
    private String email;
    private String avatar;
    private int numberOfOwnedBooks;
    private List<CartSummaryDTO> cartSummaries;
}
