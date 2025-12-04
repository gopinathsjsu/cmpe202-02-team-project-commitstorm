package com.campus.marketplace.service;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Create a category, generating id if absent.
     * @param category category entity
     * @return saved Category
     */
    public Category createCategory(Category category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID().toString());
        }
        return categoryRepository.save(category);
    }
    
    /**
     * Get category by id.
     * @param id category id
     * @return Optional category
     */
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * Get category by exact name.
     * @param name name
     * @return Optional category
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * List all categories.
     * @return list of categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Search categories by partial name.
     * @param name search term
     * @return list of categories
     */
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContaining(name);
    }
    
    /**
     * Update category fields.
     * @param category category entity
     * @return saved Category
     */
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    /**
     * Delete category by id.
     * @param id category id
     */
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
    
    /**
     * Check category name existence.
     * @param name name
     * @return true if exists
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
