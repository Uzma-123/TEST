package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.book.CategoryDTO;
import com.bookstore.dto.book.CategoryRequest;
import com.bookstore.manager.BookManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for category operations.
 *
 * <pre>
 * GET  /api/categories  — list all categories  (public)
 * POST /api/categories  — create a category    (ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final BookManager bookManager;

    public CategoryController(BookManager bookManager) {
        this.bookManager = bookManager;
    }

    /**
     * Returns all categories.
     *
     * @return {@code 200 OK} with the list of {@link CategoryDTO}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(bookManager.getAllCategories()));
    }

    /**
     * Creates a new category. Requires {@code ADMIN} role.
     *
     * @param request validated category creation request
     * @return {@code 201 Created} with the persisted {@link CategoryDTO}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        CategoryDTO created = bookManager.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", created));
    }
}
