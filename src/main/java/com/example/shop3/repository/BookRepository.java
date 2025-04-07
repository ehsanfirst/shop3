package com.example.shop3.repository;

import com.example.shop3.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book,Long> {


    int countByOwnerId(Long id);

    @Query("SELECT b FROM Book b JOIN b.tags t WHERE t.slug = :slug")
    Page<Book> findBooksByTagSlug(@Param("slug") String slug, Pageable pageable);

    Page<Book> findAllByOrderByCreatedAtDesc(Pageable pageable);
}