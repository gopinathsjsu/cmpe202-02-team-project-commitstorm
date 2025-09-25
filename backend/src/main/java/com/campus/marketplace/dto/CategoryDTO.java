package com.campus.marketplace.dto;

import com.campus.marketplace.entity.Category;
import jakarta.validation.constraints.NotBlank;

public class CategoryDTO {
    
    private String id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    // Constructors
    public CategoryDTO() {}
    
    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
