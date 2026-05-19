package org.learning.ecommerceapp.category.controller;

import org.learning.ecommerceapp.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/addCategory/{categoryName}")
    public ResponseEntity<?> addNewCategory(@PathVariable String categoryName){
        categoryService.createCategory(categoryName);
        return ResponseEntity.ok("Category Added....");
    }

    @PatchMapping("/updateCategoryName/{categoryId}")
    public ResponseEntity<?> updateCategoryName(@PathVariable long categoryId, @RequestParam String categoryName){
        categoryService.updateCategory(categoryId, categoryName);
        return ResponseEntity.accepted().build();
    }

}
