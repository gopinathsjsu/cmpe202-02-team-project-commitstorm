package com.campus.marketplace.controller;

import com.campus.marketplace.dto.CategoryDTO;
import com.campus.marketplace.entity.Category;
import com.campus.marketplace.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category endpoints: CRUD and search by name.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Create a category.
     * @param categoryDTO name
     * @return 201 with CategoryDTO
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryDTO(createdCategory));
    }
    
    /**
     * Get category by id.
     * @param id category id
     * @return 200 with CategoryDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(new CategoryDTO(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * List all categories.
     * @return 200 with list of CategoryDTO
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get category by exact name.
     * @param name category name
     * @return 200 with CategoryDTO or 404 if not found
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable String name) {
        return categoryService.getCategoryByName(name)
                .map(category -> ResponseEntity.ok(new CategoryDTO(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search categories by partial name.
     * @param name search term
     * @return 200 with list of CategoryDTO
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesByName(@RequestParam String name) {
        List<CategoryDTO> categories = categoryService.searchCategoriesByName(name).stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Update category name.
     * @param id category id
     * @param categoryDTO new name
     * @return 200 with updated CategoryDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryDTO categoryDTO) {
        return categoryService.getCategoryById(id)
                .map(existingCategory -> {
                    existingCategory.setName(categoryDTO.getName());
                    
                    Category updatedCategory = categoryService.updateCategory(existingCategory);
                    return ResponseEntity.ok(new CategoryDTO(updatedCategory));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete category.
     * @param id category id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check category name existence.
     * @param name category name
     * @return 200 with boolean exists
     */
    @GetMapping("/exists/name")
    public ResponseEntity<Boolean> checkNameExists(@RequestParam String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}
