package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.book.BookDTO;
import com.bookstore.dto.book.BookRequest;
import com.bookstore.manager.BookManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book catalog operations.
 *
 * <pre>
 * GET    /api/books          — list all books; optional ?category={id}&amp;search={query}  (public)
 * GET    /api/books/{id}     — get a single book                                        (public)
 * POST   /api/books          — create a book                                            (ADMIN)
 * PUT    /api/books/{id}     — update a book                                            (ADMIN)
 * DELETE /api/books/{id}     — delete a book                                            (ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookManager bookManager;

    public BookController(BookManager bookManager) {
        this.bookManager = bookManager;
    }

    /**
     * Returns all books. Optionally filtered by {@code category} id or {@code search} query.
     *
     * @param category optional category id as a string
     * @param search   optional search keyword (matched against title and author)
     * @return {@code 200 OK} with the list of {@link BookDTO}s
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookDTO>>> getAllBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        List<BookDTO> books = bookManager.getAllBooks(category, search);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * Returns a single book by id.
     *
     * @param id the book id
     * @return {@code 200 OK} with the {@link BookDTO}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookManager.getBookById(id)));
    }

    /**
     * Creates a new book. Requires {@code ADMIN} role.
     *
     * @param request validated book creation request
     * @return {@code 201 Created} with the persisted {@link BookDTO}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> createBook(
            @Valid @RequestBody BookRequest request) {

        BookDTO created = bookManager.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created", created));
    }

    /**
     * Updates an existing book. Requires {@code ADMIN} role.
     *
     * @param id      id of the book to update
     * @param request validated update request
     * @return {@code 200 OK} with the updated {@link BookDTO}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {

        BookDTO updated = bookManager.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated", updated));
    }

    /**
     * Deletes a book by id. Requires {@code ADMIN} role.
     *
     * @param id id of the book to delete
     * @return {@code 200 OK} with a confirmation message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookManager.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted"));
    }
}
