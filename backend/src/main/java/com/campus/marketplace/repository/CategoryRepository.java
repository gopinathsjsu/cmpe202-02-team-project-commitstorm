package com.campus.marketplace.repository;

import com.campus.marketplace.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity with basic queries.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    /** Find category by exact name. */
    Optional<Category> findByName(String name);
    
    /** Search categories by partial name. */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findByNameContaining(@Param("name") String name);
    
    /** Check if category name exists. */
    boolean existsByName(String name);
}
