package com.campus.marketplace.controller;
import org.springframework.web.bind.annotation.*; 
import org.springframework.http.ResponseEntity;
@RestController 
public class HealthController { 
    @GetMapping("/health") 
    public ResponseEntity<?> health(){ 
        return ResponseEntity.ok(java.util.Map.of("status","ok")); 
    } 
}
