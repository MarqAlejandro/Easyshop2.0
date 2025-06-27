package org.yearup.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order { //create
    private int id;
    private int userId;
    private LocalDateTime orderDate;
    private String address;
    private String city;
    private String state;
    private String zip;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;

    public Order() {
    }

    public Order(int id, int userId, LocalDateTime orderDate, String address, String city,
                 String state, String zip, BigDecimal shippingAmount, BigDecimal totalAmount) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.shippingAmount = shippingAmount;
        this.totalAmount = totalAmount;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }
}