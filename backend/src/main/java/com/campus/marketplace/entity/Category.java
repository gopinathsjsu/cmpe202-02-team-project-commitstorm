package com.campus.marketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @UuidGenerator
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "Category name is required")
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Listing> listings;
    
    // Constructors
    public Category() {}
    
    public Category(String name) {
        this.name = name;
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
    
    public List<Listing> getListings() {
        return listings;
    }
    
    public void setListings(List<Listing> listings) {
        this.listings = listings;
    }
}
