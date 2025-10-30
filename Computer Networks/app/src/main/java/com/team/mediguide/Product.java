package com.team.mediguide;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Product {

    @Exclude
    public String id;

    @PropertyName("Brand")
    public String brand;

    @PropertyName("Category_ID")
    public String categoryId;

    @PropertyName("Description")
    public String description;

    @PropertyName("Image_URL")
    public String imageUrl;

    @PropertyName("Ingredients")
    public String ingredients;

    @PropertyName("Keywords")
    public List<String> keywords;

    @PropertyName("Name")
    public String name;

    @PropertyName("Price")
    public double price;

    @PropertyName("Recommended_Usage")
    public String recommendedUsage;

    @PropertyName("Size")
    public String size;

    @PropertyName("Stock")
    public int stock;

    @PropertyName("created_at")
    public String createdAt;

    @PropertyName("is_active")
    public boolean isActive;

    @PropertyName("updated_at")
    public String updatedAt;

    // Required empty public constructor for Firestore
    public Product() {}
}