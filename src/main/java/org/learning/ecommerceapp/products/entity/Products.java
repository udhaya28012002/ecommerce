package org.learning.ecommerceapp.products.entity;

import jakarta.persistence.*;
import org.learning.ecommerceapp.category.entity.ProductCategory;
import org.learning.ecommerceapp.inventory.entity.Inventory;
import org.learning.ecommerceapp.order.entity.OrderItems;

import java.util.List;

@Entity
@Table(name = "products")
public class Products {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    private String name;

    private double price;

    private String shortDescription;

    @OneToMany(mappedBy = "products", cascade = CascadeType.ALL)
    private List<OrderItems> orderItems;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private ProductCategory productCategory;

    @OneToOne(mappedBy = "products", cascade = CascadeType.ALL)
    private Inventory inventory;

    public Products(){}

    public Products(String name, double price, String shortDescription, ProductCategory productCategory, Inventory inventory) {
        this.name = name;
        this.price = price;
        this.shortDescription = shortDescription;
        this.productCategory = productCategory;
        this.inventory = inventory;
    }

    public List<OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
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

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
