package org.learning.ecommerceapp.products.dto;

import org.learning.ecommerceapp.products.entity.ProductCategory;

public class ProductRawDto {
    private long productId;

    private String name;

    private double price;

    private String shortDescription;

    private ProductCategory productCategory;

    private int stockQuantity;

    public ProductRawDto(long productId, String name, double price, String shortDescription, ProductCategory productCategory, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.shortDescription = shortDescription;
        this.productCategory = productCategory;
        this.stockQuantity = stockQuantity;
    }

    public long getProductId() {
        return productId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}

