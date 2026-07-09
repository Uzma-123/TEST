package com.bookstore.transaction;

import com.bookstore.dao.BookDAO;
import com.bookstore.dto.book.BookRequest;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Transactional unit of work for {@link Book} operations.
 */
@Component
@Transactional
public class BookTransaction {

    private final BookDAO bookDAO;
    private final CategoryTransaction categoryTransaction;

    public BookTransaction(BookDAO bookDAO, CategoryTransaction categoryTransaction) {
        this.bookDAO = bookDAO;
        this.categoryTransaction = categoryTransaction;
    }

    /**
     * Creates and persists a new book after resolving its category.
     *
     * @param request the book creation request
     * @return the persisted {@link Book}
     */
    public Book createBook(BookRequest request) {
        Category category = categoryTransaction.getCategoryById(request.getCategoryId());

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        return bookDAO.save(book);
    }

    /**
     * Finds an existing book, applies all field updates, and saves.
     *
     * @param id      id of the book to update
     * @param request the updated field values
     * @return the updated {@link Book}
     */
    public Book updateBook(Long id, BookRequest request) {
        Book book = getBookById(id);
        Category category = categoryTransaction.getCategoryById(request.getCategoryId());

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setImageUrl(request.getImageUrl());
        book.setCategory(category);

        return bookDAO.save(book);
    }

    /**
     * Deletes a book by id, throwing {@link ResourceNotFoundException} if not found.
     *
     * @param id id of the book to delete
     */
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookDAO.delete(book);
    }

    /**
     * Retrieves a single book by id.
     *
     * @param id id of the book
     * @return the found {@link Book}
     * @throws ResourceNotFoundException if no book with that id exists
     */
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }

    /**
     * Returns all books.
     */
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    /**
     * Returns all books belonging to the given category.
     *
     * @param categoryId the category id to filter by
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksByCategory(Long categoryId) {
        return bookDAO.findByCategoryId(categoryId);
    }

    /**
     * Searches books by title and author, merging and deduplicating results
     * while preserving insertion order (title matches first, then author-only).
     *
     * @param query the search query string
     * @return deduplicated list of matching books
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        List<Book> byTitle = bookDAO.findByTitleContainingIgnoreCase(query);
        List<Book> byAuthor = bookDAO.findByAuthorContainingIgnoreCase(query);

        Set<Long> seenIds = new LinkedHashSet<>();
        List<Book> results = new ArrayList<>();

        for (Book book : byTitle) {
            if (seenIds.add(book.getId())) {
                results.add(book);
            }
        }
        for (Book book : byAuthor) {
            if (seenIds.add(book.getId())) {
                results.add(book);
            }
        }

        return results;
    }
}
