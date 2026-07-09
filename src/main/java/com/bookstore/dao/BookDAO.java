package com.bookstore.dao;

import com.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookDAO extends JpaRepository<Book, Long> {

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    Page<Book> findAll(Pageable pageable);
}
