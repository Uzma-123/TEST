package com.bookstore.manager;

import com.bookstore.dao.UserDAO;
import com.bookstore.dto.review.ReviewDTO;
import com.bookstore.dto.review.ReviewRequest;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Review;
import com.bookstore.model.User;
import com.bookstore.transaction.ReviewTransaction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewManager {

    private final ReviewTransaction reviewTransaction;
    private final UserDAO userDAO;

    public ReviewManager(ReviewTransaction reviewTransaction, UserDAO userDAO) {
        this.reviewTransaction = reviewTransaction;
        this.userDAO = userDAO;
    }

    public List<ReviewDTO> getReviewsByBook(Long bookId) {
        return reviewTransaction.getReviewsByBook(bookId)
                .stream()
                .map(this::mapToReviewDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO addReview(Long bookId, ReviewRequest request) {
        Long userId = getCurrentUserId();
        Review review = reviewTransaction.addReview(userId, bookId, request);
        return mapToReviewDTO(review);
    }

    public void deleteReview(Long bookId, Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long userId = isAdmin ? null : getCurrentUserId();
        reviewTransaction.deleteReview(reviewId, userId, isAdmin);
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private ReviewDTO mapToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setBookId(review.getBook().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserFirstName(review.getUser().getFirstName());
        dto.setUserLastName(review.getUser().getLastName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
