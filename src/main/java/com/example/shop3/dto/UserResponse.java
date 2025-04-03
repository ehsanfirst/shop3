package com.example.shop3.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

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
