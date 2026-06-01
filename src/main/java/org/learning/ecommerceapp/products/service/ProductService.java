package org.learning.ecommerceapp.products.service;

import jakarta.transaction.Transactional;
import org.learning.ecommerceapp.products.dto.ProductRawDto;
import org.learning.ecommerceapp.products.dto.request.ProductReqDto;
import org.learning.ecommerceapp.products.dto.response.AdminProductResDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Page<ProductResDto> showAllProducts(int page, int size) {

        log.debug("Fetching all products. Page: {}, Size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productPage = productRepository.findAll(pageable);

        log.info("Total products fetched: {}", productPage.getNumberOfElements());

        return productPage.map(this::convertToDto);
    }

    public Page<AdminProductResDto> showAllProductsForAdmin(int page, int size) {

        log.debug("Fetching all products for admin. Page: {}, Size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productPage = productRepository.findAll(pageable);

        log.info("Total admin products fetched: {}", productPage.getNumberOfElements());

        return productPage.map(this::convertToDtoForAdmin);
    }

    @Transactional
    public String addProduct(List<ProductReqDto> productReqDtos) {

        log.debug("Adding {} products", productReqDtos.size());

        for (ProductReqDto productReqDto : productReqDtos) {

            log.debug("Processing product: {}", productReqDto.getName());

            ProductCategory category = getProductCategory(productReqDto.getCategoryId());

            Products product = new Products(
                    productReqDto.getName(),
                    productReqDto.getPrice(),
                    productReqDto.getShortDescription(),
                    category,
                    null
            );

            Products savedProduct = productRepository.save(product);

            Inventory inventory = new Inventory();
            inventory.setProduct(savedProduct);
            inventory.setProductQuantity(productReqDto.getQuantity());

            inventoryRepository.save(inventory);

            log.info("Product added successfully with productId: {}", savedProduct.getProductId());
        }

        return "Products added successfully";
    }

    public Page<ProductResDto> listProductsBasedOnCategory(long categoryId, int page, int size) {

        log.debug("Fetching products by categoryId: {}", categoryId);

        getProductCategory(categoryId);

        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productCategoryList =
                productRepository
                        .findByProductCategoryCategoryId(categoryId, pageable);

        if (productCategoryList.isEmpty()) {

            log.warn("No products found for categoryId: {}", categoryId);

            throw new NoProductFound(
                    "No Product found under this category Id : " + categoryId
            );
        }

        log.info("Products fetched successfully for categoryId: {}", categoryId);

        return productCategoryList.map(this::convertToDto);
    }

    @CacheEvict(value = "products", key = "#productId")
    public boolean deleteProductById(long productId) {

        log.info("Deleting product with productId: {}", productId);

        if (!productRepository.existsById(productId)) {

            log.warn("Product not found for deletion. ProductId: {}", productId);

            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        productRepository.deleteById(productId);

        log.info("Product deleted successfully. ProductId: {}", productId);

        return true;
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResDto getProductById(long productId) {

        log.debug("Fetching product details for productId: {}", productId);

        Products product = validateProductPresence(productId);

        if (product == null) {

            log.warn("Product not found. ProductId: {}", productId);

            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        log.info("Product fetched successfully. ProductId: {}", productId);

        return new ProductResDto(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getShortDescription(),
                product.getProductCategory().getCategoryName(),
                mapStatus(product.getInventory().getProductQuantity())
        );
    }

    public Products getProductByIdInternal(long productId) {

        log.debug("Fetching internal product details for productId: {}", productId);

        Products product = validateProductPresence(productId);

        if (product == null) {

            log.warn("Internal product lookup failed. ProductId: {}", productId);

            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        return product;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductPrice(long productId, double price) {

        log.info("Updating product price. ProductId: {}, NewPrice: {}", productId, price);

        Products product = validateProductPresence(productId);

        if (product == null) {

            log.warn("Product not found while updating price. ProductId: {}", productId);

            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        product.setPrice(price);

        log.info("Product price updated successfully. ProductId: {}", productId);

        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductName(long productId, String name) {

        log.info("Updating product name. ProductId: {}, NewName: {}", productId, name);

        Products product = validateProductPresence(productId);

        product.setName(name);

        log.info("Product name updated successfully. ProductId: {}", productId);

        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductDescription(long productId, String description) {

        log.info("Updating product description. ProductId: {}", productId);

        Products product = validateProductPresence(productId);

        product.setShortDescription(description);

        log.info("Product description updated successfully. ProductId: {}", productId);

        return true;
    }

    @CacheEvict(value = "products", key = "#productId")
    @Transactional
    public boolean updateProductCategory(long productId, long categoryId) {

        log.info("Updating product category. ProductId: {}, CategoryId: {}", productId, categoryId);

        Products product = validateProductPresence(productId);

        ProductCategory category = getProductCategory(categoryId);

        product.setProductCategory(category);

        log.info("Product category updated successfully. ProductId: {}", productId);

        return true;
    }

    public Page<ProductResDto> filterByPrice(double minPrice, double maxPrice, int page, int size) {

        log.info("Filtering products between {} and {}", minPrice, maxPrice);

        if (minPrice <= 0 || maxPrice <= 0) {

            log.warn("Invalid price range. MinPrice: {}, MaxPrice: {}", minPrice, maxPrice);

            throw new InvalidInventoryException("Price must be greater than 0");
        }

        if (minPrice > maxPrice) {

            log.warn("Min price greater than max price");

            throw new InvalidInventoryException("minPrice cannot be greater than maxPrice");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Products> productResDtoList = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);

        if (productResDtoList.isEmpty()) {

            log.warn("No products found in price range");

            throw new NoProductFound("No Product found in this price range");
        }

        log.info("Products filtered successfully");

        return productResDtoList.map((this::convertToDto));
    }

    public Page<ProductResDto> sortByPriceAscOrDesc(boolean flag, int page, int size) {

        log.info("Sorting products by price. Ascending: {}", flag);

        return sortByProperty(flag, "price", page, size);
    }

    public Page<ProductResDto> sortByNameAscOrDesc(boolean flag, int page, int size) {

        log.info("Sorting products by name. Ascending: {}", flag);

        return sortByProperty(flag, "name", page, size);
    }

    public Page<ProductResDto> getAvailableProducts(int page, int size) {

        log.debug("Fetching available products");

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductRawDto> rawProductPage = productRepository.findAvailableProducts(pageable);

        if (rawProductPage.isEmpty()) {

            log.warn("No available products found");

            throw new NoProductFound("No Product found in this price range");
        }

        log.info("Available products fetched successfully");

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

        log.debug("Searching products with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductRawDto> productRawDtos = productRepository.findByNameContainingIgnoreCase(keyword, pageable);

        log.info("Matching products fetched successfully");

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

        log.info("Sorting products by property: {}, Ascending: {}", property, asc);

        Sort sort = asc ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Products> products = productRepository.findAll(pageable);

        if (products.isEmpty()) {

            log.warn("No products available for sorting");

            throw new NoProductFound("No products available");
        }

        log.info("Products sorted successfully");

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

    private AdminProductResDto convertToDtoForAdmin(Products product) {
        return new AdminProductResDto(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getShortDescription(),
                product.getProductCategory().getCategoryName(),
                product.getInventory().getProductQuantity());
    }

    private Products validateProductPresence(long productId) {

        log.info("Validating product presence for productId: {}", productId);

        return productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn("Product validation failed. ProductId: {}", productId);

                    return new NoProductFound("No Product found with this product id : " + productId);
                });
    }

    private ProductCategory getProductCategory(long categoryId) {

        log.info("Validating category presence for categoryId: {}", categoryId);

        return productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.warn("Category validation failed. CategoryId: {}", categoryId);

                    return new CategoryNotFoundException(
                            "No Category found with this Id : " + categoryId
                    );
                });
    }

    private Stock mapStatus(int quantity) {
        if (quantity == 0) return Stock.OUT_OF_STOCK;
        if (quantity <= 5) return Stock.LIMITED_NOS;
        return Stock.IN_STOCK;
    }
}