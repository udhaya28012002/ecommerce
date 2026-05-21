package org.learning.ecommerceapp.inventory.entity;

import jakarta.persistence.*;
import org.learning.ecommerceapp.products.entity.Products;

@Entity
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long inventoryId;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Products products;

    private int productQuantity;

    public long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Products getProduct() {
        return products;
    }

    public void setProduct(Products products) {
        this.products = products;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }
}
