package com.bookstore.transaction;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.OrderItemDAO;
import com.bookstore.dao.ReviewDAO;
import com.bookstore.dao.UserDAO;
import com.bookstore.dto.review.ReviewRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.model.Book;
import com.bookstore.model.Review;
import com.bookstore.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class ReviewTransaction {

    private final ReviewDAO reviewDAO;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final OrderItemDAO orderItemDAO;

    public ReviewTransaction(ReviewDAO reviewDAO, UserDAO userDAO,
                             BookDAO bookDAO, OrderItemDAO orderItemDAO) {
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
        this.bookDAO = bookDAO;
        this.orderItemDAO = orderItemDAO;
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByBook(Long bookId) {
        return reviewDAO.findByBookId(bookId);
    }

    public Review addReview(Long userId, Long bookId, ReviewRequest request) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book = bookDAO.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!orderItemDAO.existsByUserIdAndBookIdAndOrderDelivered(userId, bookId)) {
            throw new BadRequestException("You can only review books you have purchased and received");
        }

        if (reviewDAO.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("You have already reviewed this book");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .book(book)
                .build();

        return reviewDAO.save(review);
    }

    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        Review review = reviewDAO.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this review");
        }

        reviewDAO.delete(review);
    }
}
