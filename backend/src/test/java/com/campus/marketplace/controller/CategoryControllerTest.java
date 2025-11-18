package com.campus.marketplace.controller;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {
    
    @Mock
    private CategoryService categoryService;
    
    @InjectMocks
    private CategoryController categoryController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();
        
        testCategory = new Category();
        testCategory.setId("category-123");
        testCategory.setName("Electronics");
    }
    
    @Test
    void testGetCategoryById_Found() throws Exception {
        when(categoryService.getCategoryById("category-123")).thenReturn(Optional.of(testCategory));

        mockMvc.perform(get("/api/categories/category-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("category-123"))
            .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService, times(1)).getCategoryById("category-123");
    }
    
    @Test
    void testGetCategoryById_NotFound() throws Exception {
        when(categoryService.getCategoryById("non-existent")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/categories/non-existent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetAllCategories() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);
        
        mockMvc.perform(get("/api/categories")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"));
    }
    
    @Test
    void testGetCategoryByName() throws Exception {
        when(categoryService.getCategoryByName("Electronics")).thenReturn(Optional.of(testCategory));
        
        mockMvc.perform(get("/api/categories/name/Electronics")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Electronics"));
    }
    
    @Test
    void testSearchCategories() throws Exception {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.searchCategoriesByName("Electro")).thenReturn(categories);

        mockMvc.perform(get("/api/categories/search?name=Electro")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"));
    }
    
    @Test
    void testCreateCategory() throws Exception {
        when(categoryService.createCategory(any(Category.class))).thenReturn(testCategory);
        
        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testCategory)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Electronics"));
    }
    
    @Test
    void testUpdateCategory() throws Exception {
        testCategory.setName("Updated Electronics");
        when(categoryService.updateCategory(any(Category.class))).thenReturn(testCategory);
        Category existing = new Category();
        existing.setId("category-123");
        existing.setName("Electronics");
        when(categoryService.getCategoryById("category-123")).thenReturn(Optional.of(existing));
        
        mockMvc.perform(put("/api/categories/category-123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testCategory)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }
    
    @Test
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/category-123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory("category-123");
    }
    
    @Test
    void testCategoryExists() throws Exception {
        when(categoryService.existsByName("Electronics")).thenReturn(true);

        mockMvc.perform(get("/api/categories/exists/name?name=Electronics")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
