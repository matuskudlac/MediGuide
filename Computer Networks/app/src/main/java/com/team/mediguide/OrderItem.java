package com.team.mediguide;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class OrderItem implements Serializable {
    
    @PropertyName("Product_ID")
    public String productId;
    
    @PropertyName("Product_Name")
    public String productName;
    
    @PropertyName("Image_URL")
    public String imageUrl;
    
    @PropertyName("Price")
    public double price;
    
    @PropertyName("Quantity")
    public int quantity;
    
    // Required for Firestore
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, String imageUrl, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }
    
    public double getLineTotal() {
        return price * quantity;
    }
}
