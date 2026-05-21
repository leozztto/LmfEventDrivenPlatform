package com.lmf.order.orderservice.domain.model.customer;

public class ShippingAddress {

    private final String street;

    private final String number;

    private final String city;

    private final String zipCode;

    private final String country;

    public ShippingAddress(String street, String number, String city, String zipCode, String country) {

        this.street = street;
        this.number = number;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }
}
