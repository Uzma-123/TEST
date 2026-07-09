package com.bookstore.manager;

import com.bookstore.dto.book.BookDTO;
import com.bookstore.dto.book.BookRequest;
import com.bookstore.dto.book.CategoryDTO;
import com.bookstore.dto.book.CategoryRequest;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.transaction.BookTransaction;
import com.bookstore.transaction.CategoryTransaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates book and category business logic.
 *
 * <p>Calls {@link BookTransaction} and {@link CategoryTransaction} for all data
 * access, and maps the resulting entities to their respective response DTOs.</p>
 */
@Service
public class BookManager {

    private final BookTransaction bookTransaction;
    private final CategoryTransaction categoryTransaction;

    public BookManager(BookTransaction bookTransaction, CategoryTransaction categoryTransaction) {
        this.bookTransaction = bookTransaction;
        this.categoryTransaction = categoryTransaction;
    }

    // -----------------------------------------------------------------------
    // Book operations
    // -----------------------------------------------------------------------

    /**
     * Creates a new book.
     *
     * @param request validated creation request
     * @return {@link BookDTO} of the persisted book
     */
    public BookDTO createBook(BookRequest request) {
        return mapToBookDTO(bookTransaction.createBook(request));
    }

    /**
     * Updates an existing book.
     *
     * @param id      id of the book to update
     * @param request validated update request
     * @return {@link BookDTO} of the updated book
     */
    public BookDTO updateBook(Long id, BookRequest request) {
        return mapToBookDTO(bookTransaction.updateBook(id, request));
    }

    /**
     * Deletes a book by id.
     *
     * @param id id of the book to delete
     */
    public void deleteBook(Long id) {
        bookTransaction.deleteBook(id);
    }

    /**
     * Returns a single book by id.
     *
     * @param id id of the book
     * @return {@link BookDTO} of the found book
     */
    public BookDTO getBookById(Long id) {
        return mapToBookDTO(bookTransaction.getBookById(id));
    }

    /**
     * Returns books filtered by category or search query, or all books when no
     * filter is supplied.
     *
     * <ul>
     *   <li>If {@code category} is non-null: filter by category id.</li>
     *   <li>Else if {@code search} is non-null: full-text search on title + author.</li>
     *   <li>Otherwise: return all books.</li>
     * </ul>
     *
     * @param category optional category id (as a string from the query param)
     * @param search   optional search query
     * @return list of matching {@link BookDTO}s
     */
    public List<BookDTO> getAllBooks(String category, String search) {
        List<Book> books;

        if (category != null && !category.isBlank()) {
            books = bookTransaction.getBooksByCategory(Long.parseLong(category));
        } else if (search != null && !search.isBlank()) {
            books = bookTransaction.searchBooks(search);
        } else {
            books = bookTransaction.getAllBooks();
        }

        return books.stream()
                .map(this::mapToBookDTO)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Category operations
    // -----------------------------------------------------------------------

    /**
     * Returns all categories.
     *
     * @return list of {@link CategoryDTO}s
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryTransaction.getAllCategories().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new category.
     *
     * @param request validated creation request
     * @return {@link CategoryDTO} of the persisted category
     */
    public CategoryDTO createCategory(CategoryRequest request) {
        return mapToCategoryDTO(categoryTransaction.createCategory(request));
    }

    // -----------------------------------------------------------------------
    // Private mapping helpers
    // -----------------------------------------------------------------------

    private BookDTO mapToBookDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setStockQuantity(book.getStockQuantity());
        dto.setImageUrl(book.getImageUrl());
        dto.setCreatedAt(book.getCreatedAt());
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }

    private CategoryDTO mapToCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
