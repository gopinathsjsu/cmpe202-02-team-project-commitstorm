package com.campus.marketplace.config;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.CategoryService;
import com.campus.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeCategories();
    }

    private void initializeUsers() {
        // Create admin user
        if (!userService.existsByEmail("admin@campusmarket.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@campusmarket.com");
            admin.setRole(User.UserRole.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            userService.createUser(admin);
        }

        // Create sample users
        if (!userService.existsByEmail("john.doe@university.edu")) {
            User user1 = new User();
            user1.setName("John Doe");
            user1.setEmail("john.doe@university.edu");
            user1.setRole(User.UserRole.USER);
            user1.setStatus(User.UserStatus.ACTIVE);
            userService.createUser(user1);
        }

        if (!userService.existsByEmail("jane.smith@university.edu")) {
            User user2 = new User();
            user2.setName("Jane Smith");
            user2.setEmail("jane.smith@university.edu");
            user2.setRole(User.UserRole.USER);
            user2.setStatus(User.UserStatus.ACTIVE);
            userService.createUser(user2);
        }
    }

    private void initializeCategories() {
        String[] categories = {
            "Electronics", "Books", "Clothing", "Furniture", "Sports",
            "Home & Garden", "Automotive", "Toys & Games", "Health & Beauty",
            "Musical Instruments", "Art & Crafts", "Office Supplies"
        };

        for (String categoryName : categories) {
            if (!categoryService.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                categoryService.createCategory(category);
            }
        }
    }
}
