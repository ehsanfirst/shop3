package com.example.shop3.controller;

import com.example.shop3.dto.BookDTO;
import com.example.shop3.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("categories")
@Controller
@RequiredArgsConstructor
public class TagsController {

    private final BookService bookService;

@RequestMapping("/{slug}")
    public String slug(
        @PathVariable String slug,
        // PageableDefault: صفحه پیش‌فرض ۰، اندازه ۱۰، مرتب‌سازی بر اساس نام کتاب صعودی (می‌تونی تغییر بدی)
        @PageableDefault(size = 10, sort = "name") Pageable pageable,
            Model model) {

    Page<BookDTO> books =  bookService.getBooksByTagSlug(slug, pageable);
    model.addAttribute("books", books);
    return "categories";


}

}
