package com.campus.marketplace.service;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId("category-123");
        testCategory.setName("Electronics");
    }
    
    @Test
    void testCreateCategory_WithoutId_GeneratesUUID() {
        Category category = new Category();
        category.setName("Books");
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            assertNotNull(savedCategory.getId());
            return savedCategory;
        });
        
        Category result = categoryService.createCategory(category);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
    
    @Test
    void testCreateCategory_WithId_DoesNotOverrideId() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        Category result = categoryService.createCategory(testCategory);
        
        assertEquals("category-123", result.getId());
        verify(categoryRepository, times(1)).save(testCategory);
    }
    
    @Test
    void testGetCategoryById_Found() {
        when(categoryRepository.findById("category-123")).thenReturn(Optional.of(testCategory));
        
        Optional<Category> result = categoryService.getCategoryById("category-123");
        
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
        verify(categoryRepository, times(1)).findById("category-123");
    }
    
    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        Optional<Category> result = categoryService.getCategoryById("non-existent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetCategoryByName_Found() {
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));
        
        Optional<Category> result = categoryService.getCategoryByName("Electronics");
        
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }
    
    @Test
    void testGetCategoryByName_NotFound() {
        when(categoryRepository.findByName("NonExistent")).thenReturn(Optional.empty());
        
        Optional<Category> result = categoryService.getCategoryByName("NonExistent");
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetAllCategories() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);
        
        List<Category> result = categoryService.getAllCategories();
        
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findAll();
    }
    
    @Test
    void testSearchCategories() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByNameContaining("Electro")).thenReturn(categories);
        
        List<Category> result = categoryService.searchCategoriesByName("Electro");
        
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findByNameContaining("Electro");
    }
    
    @Test
    void testUpdateCategory() {
        testCategory.setName("Updated Electronics");
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        
        Category result = categoryService.updateCategory(testCategory);
        
        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository, times(1)).save(testCategory);
    }
    
    @Test
    void testDeleteCategory() {
        categoryService.deleteCategory("category-123");
        
        verify(categoryRepository, times(1)).deleteById("category-123");
    }
    
    @Test
    void testCategoryExists_True() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        
        boolean result = categoryService.existsByName("Electronics");
        
        assertTrue(result);
    }
    
    @Test
    void testCategoryExists_False() {
        when(categoryRepository.existsByName("NonExistent")).thenReturn(false);
        
        boolean result = categoryService.existsByName("NonExistent");
        
        assertFalse(result);
    }
}
