package com.team.mediguide;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Order {
    
    @PropertyName("Order_ID")
    public String orderId;
    
    @PropertyName("User_ID")
    public String userId;
    
    @PropertyName("Items")
    public List<OrderItem> items;
    
    @PropertyName("Subtotal")
    public double subtotal;
    
    @PropertyName("Tax")
    public double tax;
    
    @PropertyName("Total")
    public double total;
    
    @PropertyName("Payment_Status")
    public String paymentStatus;
    
    @PropertyName("Payment_Method")
    public String paymentMethod;
    
    @ServerTimestamp
    @PropertyName("Order_Date")
    public Date orderDate;
    
    // Required for Firestore
    public Order() {}
    
    public Order(String orderId, String userId, List<OrderItem> items, double subtotal, double tax, double total, String paymentStatus, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }
}
