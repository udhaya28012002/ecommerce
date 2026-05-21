package org.learning.ecommerceapp.products.controller;

import org.learning.ecommerceapp.products.dto.request.ProductReqDto;
import org.learning.ecommerceapp.products.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping("/addProduct")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProduct(@RequestBody ProductReqDto productReqDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(productReqDto));
    }

    @GetMapping("/listAllProducts")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getAllProducts(){
        return ResponseEntity.ok(productService.showAllProducts());
    }

    @GetMapping("/listProductByCategory/{categoryId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getProductsByCategory(@PathVariable long categoryId){
        return ResponseEntity.ok(productService.listProductsBasedOnCategory(categoryId));
    }

    @DeleteMapping("/deleteProduct/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable long productId){
        return ResponseEntity.ok(productService.deleteProductById(productId));
    }

    @GetMapping("/getProduct/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getProduct(@PathVariable long productId){
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PatchMapping("/updateProduct/price/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductPrice(@PathVariable long productId, @RequestParam double updatedPrice){
        return ResponseEntity.ok(productService.updateProductPrice(productId, updatedPrice));
    }

    @PatchMapping("/updateProduct/name/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductName(@PathVariable long productId, @RequestParam String updatedName){
        return ResponseEntity.ok(productService.updateProductName(productId, updatedName));
    }

    @PatchMapping("/updateProduct/description/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductDesc(@PathVariable long productId, @RequestParam String newDescription){
        return ResponseEntity.ok(productService.updateProductDescription(productId, newDescription));
    }

    @PatchMapping("/updateProduct/category/{productId}/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductCategory(@PathVariable long productId, @PathVariable long categoryId){
        return ResponseEntity.ok(productService.updateProductCategory(productId, categoryId));
    }

    @GetMapping("/filterProducts")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> filterProducts(@RequestParam  int minPrice, @RequestParam int maxPrice){
        return ResponseEntity.ok(productService.filterByPrice(minPrice, maxPrice));
    }

    @GetMapping("/sortByPrice/{asc}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> sortByPrice(@PathVariable boolean asc){
        return ResponseEntity.ok(productService.sortByPriceAscOrDesc(asc));
    }

    @GetMapping("/sortByName/{asc}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> sortByName(@PathVariable boolean asc){
        return ResponseEntity.ok(productService.sortByNameAscOrDesc(asc));
    }

    @GetMapping("/getInStockProducts")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getAvailableProducts(){
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/findByMatchingName/{matchingCase}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<?> getProductsByMatchingCase(@PathVariable String matchingCase){
        return ResponseEntity.ok(productService.findByNameContainingIgnoreCase(matchingCase));
    }



}
