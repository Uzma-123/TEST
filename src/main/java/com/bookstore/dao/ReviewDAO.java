package com.bookstore.dao;

import com.bookstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDAO extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    List<Review> findByUserId(Long userId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
