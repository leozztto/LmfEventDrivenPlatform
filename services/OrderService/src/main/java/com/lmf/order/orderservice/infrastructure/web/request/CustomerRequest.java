package com.lmf.order.orderservice.infrastructure.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CustomerRequest(

        UUID customerId,

        @NotBlank(message = "Customer name is required") String name,

        @Email(message = "Invalid customer email") String email,

        @NotBlank(message = "Customer phone is required") String phone) {
}
