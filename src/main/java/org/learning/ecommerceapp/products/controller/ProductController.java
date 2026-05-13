package org.learning.ecommerceapp.products.controller;

import org.learning.ecommerceapp.products.dto.request.ProductReqDto;
import org.learning.ecommerceapp.products.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProduct(@RequestBody ProductReqDto productReqDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(productReqDto));
    }

    @GetMapping("/listAllProducts")
    public ResponseEntity<?> getAllProducts(){
        return ResponseEntity.ok(productService.showAllProducts());
    }

    @GetMapping("/listProductByCategory/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable long categoryId){
        return ResponseEntity.ok(productService.listProductsBasedOnCategory(categoryId));
    }

    @DeleteMapping("/deleteProduct/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable long productId){
        return ResponseEntity.ok(productService.deleteProductById(productId));
    }

    @GetMapping("/getProduct/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable long productId){
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PatchMapping("/updateProduct/price/{productId}")
    public ResponseEntity<?> updateProductPrice(@PathVariable long productId, @RequestParam double updatedPrice){
        return ResponseEntity.ok(productService.updateProductPrice(productId, updatedPrice));
    }

    @PatchMapping("/updateProduct/name/{productId}")
    public ResponseEntity<?> updateProductName(@PathVariable long productId, @RequestParam String updatedName){
        return ResponseEntity.ok(productService.updateProductName(productId, updatedName));
    }

    @PatchMapping("/updateProduct/description/{productId}")
    public ResponseEntity<?> updateProductDesc(@PathVariable long productId, @RequestParam String newDescription){
        return ResponseEntity.ok(productService.updateProductDescription(productId, newDescription));
    }

    @PatchMapping("/updateProduct/category/{productId}/{categoryId}")
    public ResponseEntity<?> updateProductCategory(@PathVariable long productId, @PathVariable long categoryId){
        return ResponseEntity.ok(productService.updateProductCategory(productId, categoryId));
    }

    @PatchMapping("/updateInventory/{productId}")
    public ResponseEntity<?> updateInventoryQuantity(@PathVariable long productId, @RequestParam int quantity, @RequestParam boolean positive){
        return ResponseEntity.ok(productService.updateInventoryQuantity(productId, quantity, positive));
    }

    @GetMapping("/filterProducts")
    public ResponseEntity<?> filterProducts(@RequestParam  int minPrice, @RequestParam int maxPrice){
        return ResponseEntity.ok(productService.filterByPrice(minPrice, maxPrice));
    }

    @GetMapping("/sortByPrice/{asc}")
    public ResponseEntity<?> sortByPrice(@PathVariable boolean flag){
        return ResponseEntity.ok(productService.sortByPriceAscOrDesc(flag));
    }

    @GetMapping("/sortByName/{asc}")
    public ResponseEntity<?> sortByName(@PathVariable boolean flag){
        return ResponseEntity.ok(productService.sortByNameAscOrDesc(flag));
    }

    @GetMapping("/getInStockProducts")
    public ResponseEntity<?> getAvailableProducts(){
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @GetMapping("/findByMatchingName/{matchingCase}")
    public ResponseEntity<?> getProductsByMatchingCase(@PathVariable String matchingCase){
        return ResponseEntity.ok(productService.findByNameContainingIgnoreCase(matchingCase));
    }



}
