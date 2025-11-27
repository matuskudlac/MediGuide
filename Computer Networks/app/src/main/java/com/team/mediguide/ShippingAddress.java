package com.team.mediguide;

import com.google.firebase.firestore.PropertyName;

public class ShippingAddress {
    
    @PropertyName("Street")
    public String street;
    
    @PropertyName("City")
    public String city;
    
    @PropertyName("State")
    public String state;
    
    @PropertyName("Zip_Code")
    public String zipCode;
    
    @PropertyName("Country")
    public String country;
    
    // Required for Firestore
    public ShippingAddress() {}
    
    public ShippingAddress(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    public String getFullAddress() {
        return street + "\n" + city + ", " + state + " " + zipCode + "\n" + country;
    }
}
