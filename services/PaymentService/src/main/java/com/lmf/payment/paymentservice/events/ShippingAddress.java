package com.lmf.payment.paymentservice.events;

public record ShippingAddress(

        String street,

        String number,

        String city,

        String state,

        String zipCode) {
}
