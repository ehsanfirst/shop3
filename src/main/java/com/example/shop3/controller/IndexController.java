package com.example.shop3.controller;


import com.example.shop3.dto.BookDTO;
import com.example.shop3.repository.BookRepository;
import com.example.shop3.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private  final BookService bookService;

    @GetMapping
    public String index(Model model) {
        Pageable pageable = PageRequest.of(0, 5); // فقط 5 تای اول

        Page<BookDTO> newestBooks = bookService.getNewestBooks(pageable); // فرض: متد سرویس ساخته شده
        model.addAttribute("newestBooks", newestBooks);


        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

//    @GetMapping("/register")
//    public String signup() {
//        return "register";
//    }
}
