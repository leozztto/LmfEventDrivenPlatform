package com.lmf.fraud.fraudservice.infrastructure.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateBlocklistEntryRequest(

        UUID customerId,

        @Email
        String customerEmail,

        @NotBlank
        String reason) {
}
