package org.learning.ecommerceapp.products.service;

import jakarta.transaction.Transactional;
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

    public List<ProductResDto> showAllProducts() {
        return structureDto(productRepository.findAll());

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

    public List<ProductResDto> listProductsBasedOnCategory(long categoryId) {

        getProductCategory(categoryId);   // validates category exists

        List<Products> productCategoryList =
                productRepository
                        .findByProductCategoryCategoryId(categoryId);

        if (productCategoryList.isEmpty()) {
            throw new NoProductFound(
                    "No Product found under this category Id : " + categoryId
            );
        }

        return structureDto(productCategoryList);
    }

    public boolean deleteProductById(long productId) {
        //Need to validate if the user is admin:

        if (!productRepository.existsById(productId)) {
            throw new NoProductFound("No Product found with this product id : " + productId);
        }

        productRepository.deleteById(productId);
        return true;
    }

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

    @Transactional
    public boolean updateProductPrice(long productId, double price) {
        Products product = validateProductPresence(productId);

        if (product == null)
            throw new NoProductFound("No Product found with this product id : " + productId);

        product.setPrice(price);
        return true;
    }

    @Transactional
    public boolean updateProductName(long productId, String name) {
        Products product = validateProductPresence(productId);
        product.setName(name);
        return true;
    }

    @Transactional
    public boolean updateProductDescription(long productId, String description) {
        Products product = validateProductPresence(productId);
        product.setShortDescription(description);
        return true;
    }

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

    public List<ProductResDto> sortByPriceAscOrDesc(boolean flag) {
        return sortByProperty(flag, "price");
    }

    /*public List<ProductResDto> sortByPriceDesc(){
        return sortByProperty(false, "price");
    }*/
    public List<ProductResDto> sortByNameAscOrDesc(boolean flag) {
        return sortByProperty(flag, "name");
    }

    /*public List<ProductResDto> sortByNameDesc(){
        return sortByProperty(false, "name");
    }*/

    public List<ProductResDto> getAvailableProducts() {
        List<ProductResDto> productResDtoList = productRepository.findAvailableProducts()
                .stream()
                .map(productRawDto -> new ProductResDto(
                        productRawDto.getProductId(),
                        productRawDto.getName(),
                        productRawDto.getPrice(),
                        productRawDto.getShortDescription(),
                        productRawDto.getProductCategory().getCategoryName(),
                        mapStatus(productRawDto.getStockQuantity())
                ))
                .toList();

        if (productResDtoList.isEmpty()) {
            throw new NoProductFound("No Product found in this price range");
        }

        return productResDtoList;
    }

    public List<ProductResDto> findByNameContainingIgnoreCase(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(productRawDto -> new ProductResDto(
                        productRawDto.getProductId(),
                        productRawDto.getName(),
                        productRawDto.getPrice(),
                        productRawDto.getShortDescription(),
                        productRawDto.getProductCategory().getCategoryName(),
                        mapStatus(productRawDto.getStockQuantity())
                ))
                .toList();
    }

    private List<ProductResDto> sortByProperty(boolean asc, String property) {

        Sort sort = asc ? Sort.by(property).ascending()
                : Sort.by(property).descending();

        List<ProductResDto> productResDtoList = structureDto(productRepository.findAll(sort));

        if (productResDtoList.isEmpty()) {
            throw new NoProductFound("No products available");
        }

        return productResDtoList;
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
