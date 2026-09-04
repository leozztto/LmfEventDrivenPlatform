package com.lmf.platform.contracts;

import java.util.UUID;

public record CustomerInfo(

        UUID customerId,

        String name,

        String email,

        String phone) {
}
