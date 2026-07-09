package com.bookstore.dto.book;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a category.
 */
public class CategoryRequest {

    @NotBlank
    private String name;

    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
