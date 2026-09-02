package com.lmf.platform.contracts;

public record ShippingAddress(

        String street,

        String number,

        String city,

        String zipCode,

        String country) {
}
