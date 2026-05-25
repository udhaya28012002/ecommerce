package org.learning.ecommerceapp.products.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.products.dto.ProductRawDto;
import org.learning.ecommerceapp.products.dto.request.ProductReqDto;
import org.learning.ecommerceapp.products.dto.response.ProductResDto;
import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.learning.ecommerceapp.category.entity.ProductCategory;
import org.learning.ecommerceapp.products.entity.Products;
import org.learning.ecommerceapp.products.entity.Stock;
import org.learning.ecommerceapp.category.exception.CategoryNotFoundException;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.learning.ecommerceapp.inventory.repository.InventoryRepository;
import org.learning.ecommerceapp.category.repository.ProductCategoryRepository;
import org.learning.ecommerceapp.products.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Page<ProductResDto> showAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productPage = productRepository.findAll(pageable);

        return productPage.map(this::convertToDto);
    }


    public String addProduct(ProductReqDto productReqDto) {

        //Need to validate if the user is of Admin:
        Products product = new Products(
                productReqDto.getName(),
                productReqDto.getPrice(),
                productReqDto.getShortDescription(),
                getProductCategory(productReqDto.getCategoryId()),
                null
        );
        productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setProductQuantity(productReqDto.getQuantity());

        inventoryRepository.save(inventory);

        return "Successfully added";
    }

    public Page<ProductResDto> listProductsBasedOnCategory(long categoryId, int page, int size) {

        getProductCategory(categoryId);   // validates category exists

        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productCategoryList =
                productRepository
                        .findByProductCategoryCategoryId(categoryId, pageable);

        if (productCategoryList.isEmpty()) {
            throw new NoProductFound(
                    "No Product found under this category Id : " + categoryId
            );
        }

        return productCategoryList.map(this::convertToDto);
    }

    @CacheEvict(value = "products", key = "#productId")
    public boolean deleteProductById(long productId) {
        //Need to validate if the user is admin:

        if (!productRepository.existsById(productId)) {
            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        productRepository.deleteById(productId);
        return true;
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResDto getProductById(long productId) {

        Products product = validateProductPresence(productId);

        if (product == null)
            throw new NoProductFound("No Product found with this product id : " + productId);

        return new ProductResDto(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getShortDescription(),
                product.getProductCategory().getCategoryName(),
                mapStatus(product.getInventory().getProductQuantity())
        );
    }

    //For Internal Use : No Controllers
    public Products getProductByIdInternal(long productId) {

        Products product = validateProductPresence(productId);

        if (product == null)
            throw new NoProductFound("No Product found with this product id : " + productId);

        return product;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductPrice(long productId, double price) {
        Products product = validateProductPresence(productId);

        if (product == null)
            throw new NoProductFound("No Product found with this product id : " + productId);

        product.setPrice(price);
        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductName(long productId, String name) {
        Products product = validateProductPresence(productId);
        product.setName(name);
        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductDescription(long productId, String description) {
        Products product = validateProductPresence(productId);
        product.setShortDescription(description);
        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductCategory(long productId, long categoryId) {
        Products product = validateProductPresence(productId);
        ProductCategory category = getProductCategory(categoryId);
        product.setProductCategory(category);
        return true;
    }

    public List<ProductResDto> filterByPrice(double minPrice, double maxPrice) {
        if (minPrice <= 0 || maxPrice <= 0) {
            throw new InvalidInventoryException("Price must be greater than 0");
        }

        if (minPrice > maxPrice) {
            throw new InvalidInventoryException("minPrice cannot be greater than maxPrice");
        }

        List<ProductResDto> productResDtoList = structureDto(productRepository.findByPriceBetween(minPrice, maxPrice));

        if (productResDtoList.isEmpty()) {
            throw new NoProductFound("No Product found in this price range");
        }

        return productResDtoList;
    }

    public Page<ProductResDto> sortByPriceAscOrDesc(boolean flag, int page, int size) {
        return sortByProperty(flag, "price", page, size);
    }

    public Page<ProductResDto> sortByNameAscOrDesc(boolean flag, int page, int size) {
        return sortByProperty(flag, "name", page, size);
    }

    public Page<ProductResDto> getAvailableProducts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductRawDto> rawProductPage = productRepository.findAvailableProducts(pageable);

        if (rawProductPage.isEmpty()) {
            throw new NoProductFound("No Product found in this price range");
        }

        return rawProductPage.map(productRawDto1 -> new ProductResDto(
                productRawDto1.getProductId(),
                productRawDto1.getName(),
                productRawDto1.getPrice(),
                productRawDto1.getShortDescription(),
                productRawDto1.getProductCategory().getCategoryName(),
                mapStatus(productRawDto1.getStockQuantity())
        ));
    }

    public Page<ProductResDto> findByNameContainingIgnoreCase(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductRawDto> productRawDtos = productRepository.findByNameContainingIgnoreCase(keyword, pageable);

        return productRawDtos.map(productRawDto -> new ProductResDto(
                productRawDto.getProductId(),
                productRawDto.getName(),
                productRawDto.getPrice(),
                productRawDto.getShortDescription(),
                productRawDto.getProductCategory().getCategoryName(),
                mapStatus(productRawDto.getStockQuantity())
        ));
    }

    private Page<ProductResDto> sortByProperty(boolean asc, String property, int page, int size) {


        Sort sort = asc ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Products> products = productRepository.findAll(pageable);

        if (products.isEmpty()) {
            throw new NoProductFound("No products available");
        }

        return products.map(this::convertToDto);
    }

    private List<ProductResDto> structureDto(List<Products> productList) {
        return productList
                .stream()
                .map(product -> new ProductResDto(
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getShortDescription(),
                        product.getProductCategory().getCategoryName(),
                        mapStatus(product.getInventory().getProductQuantity())
                ))
                .toList();
    }

    private ProductResDto convertToDto(Products product) {
        return new ProductResDto(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getShortDescription(),
                product.getProductCategory().getCategoryName(),
                mapStatus(product.getInventory().getProductQuantity()));
    }

    private Products validateProductPresence(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new NoProductFound("No Product found with this product id : " + productId)
                );
    }

    private ProductCategory getProductCategory(long categoryId) {
        return productCategoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "No Category found with this Id : " + categoryId
                        ));
    }

    private Stock mapStatus(int quantity) {
        if (quantity == 0) return Stock.OUT_OF_STOCK;
        if (quantity <= 5) return Stock.LIMITED_NOS;
        return Stock.IN_STOCK;
    }
}
