package com.example.shop3.controller;

import com.example.shop3.dto.BookResponse;
import com.example.shop3.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("books")
@RequiredArgsConstructor
public class ShowBook {

    private final BookService bookService;

@GetMapping("/{id}")
    public String showBook(Model model, @PathVariable Long id){

    BookResponse bookresponse = bookService.getBookById(id);
    model.addAttribute("book", bookresponse);
    return "books/book";
}

}
