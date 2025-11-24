package com.team.mediguide;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class CartItem {

    @Exclude
    public String id;

    @PropertyName("Product_ID")
    public String productId;

    @PropertyName("Quantity")
    public int quantity;

    @PropertyName("Date_Added")
    public Date dateAdded;

    // Required for Firestore
    public CartItem() {}
}