package com.bookstore.transaction;

import com.bookstore.dao.CategoryDAO;
import com.bookstore.dto.book.CategoryRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Category;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transactional unit of work for {@link Category} operations.
 */
@Component
@Transactional
public class CategoryTransaction {

    private final CategoryDAO categoryDAO;

    public CategoryTransaction(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * Creates a new category after verifying name uniqueness.
     *
     * @param request the category creation request
     * @return the persisted {@link Category}
     * @throws BadRequestException if a category with the same name already exists
     */
    public Category createCategory(CategoryRequest request) {
        categoryDAO.findByName(request.getName()).ifPresent(c -> {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        });

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return categoryDAO.save(category);
    }

    /**
     * Returns all categories.
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    /**
     * Returns a single category by id, or throws {@link ResourceNotFoundException}.
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}
