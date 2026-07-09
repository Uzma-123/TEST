package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.review.ReviewDTO;
import com.bookstore.dto.review.ReviewRequest;
import com.bookstore.manager.ReviewManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for review operations.
 *
 * <pre>
 * GET    /api/books/{bookId}/reviews              — get all reviews for a book (public)
 * POST   /api/books/{bookId}/reviews              — submit a review (USER, must have purchased)
 * DELETE /api/books/{bookId}/reviews/{reviewId}   — delete a review (owner or ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/api/books/{bookId}/reviews")
public class ReviewController {

    private final ReviewManager reviewManager;

    public ReviewController(ReviewManager reviewManager) {
        this.reviewManager = reviewManager;
    }

    /**
     * Returns all reviews for a given book. Public endpoint — no authentication required.
     *
     * @param bookId id of the book
     * @return {@code 200 OK} with the list of {@link ReviewDTO}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getReviewsByBook(@PathVariable Long bookId) {
        List<ReviewDTO> reviews = reviewManager.getReviewsByBook(bookId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Submits a review for a book. Requires authentication.
     * The user must have purchased and received the book before reviewing.
     *
     * @param bookId  id of the book to review
     * @param request validated review payload
     * @return {@code 201 Created} with the created {@link ReviewDTO}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewDTO dto = reviewManager.addReview(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", dto));
    }

    /**
     * Deletes a review. The review owner or an ADMIN may delete it.
     *
     * @param bookId   id of the book (used for route scoping)
     * @param reviewId id of the review to delete
     * @return {@code 200 OK} with a confirmation message
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long bookId,
            @PathVariable Long reviewId) {

        reviewManager.deleteReview(bookId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }
}
