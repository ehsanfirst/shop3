package com.example.shop3.repository;

import com.example.shop3.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {


    int countByOwnerId(Long id);
}
