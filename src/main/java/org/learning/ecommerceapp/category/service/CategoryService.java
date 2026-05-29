package org.learning.ecommerceapp.category.service;

import org.learning.ecommerceapp.category.dto.CategoryResDto;
import org.learning.ecommerceapp.category.entity.ProductCategory;
import org.learning.ecommerceapp.category.repository.ProductCategoryRepository;
import org.learning.ecommerceapp.category.exception.CategoryAlreadyExistsException;
import org.learning.ecommerceapp.category.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public CategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    public void createCategory(String categoryName) {

        boolean exists = productCategoryRepository.existsByCategoryNameIgnoreCase(categoryName);

        if (exists) {
            throw new CategoryAlreadyExistsException("Category Already Exists");
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setCategoryName(categoryName);
        productCategoryRepository.save(productCategory);
    }

    @Transactional
    public void updateCategory(long oldCategoryId, String newCategoryName) {

        ProductCategory pc = productCategoryRepository
                .findById(oldCategoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "No Category with this Id : " + oldCategoryId
                        )
                );

        pc.setCategoryName(newCategoryName);
    }

    public ProductCategory getCategoryById(long categoryId) {
        return productCategoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "No Category with this Id : " + categoryId
                        )
                );
    }

    public List<CategoryResDto> getAllCategories() {
        List<ProductCategory> list = productCategoryRepository.findAll();

        if (list.isEmpty()) {
            throw new CategoryNotFoundException("No Category found");
        }

        return list.stream()
                .map((productCategory) -> {
                            CategoryResDto categoryResDto = new CategoryResDto();
                            categoryResDto.setCategoryId(productCategory.getCategoryId());
                            categoryResDto.setCategoryName(productCategory.getCategoryName());
                            return categoryResDto;
                        }
                ).toList();
    }


}
