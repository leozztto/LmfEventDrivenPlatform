package com.lmf.inventory.inventoryservice.domain.event.order;

import java.util.UUID;

public class CustomerInfo {

    private final UUID customerId;

    private final String name;

    private final String email;

    private final String phone;

    public CustomerInfo(UUID customerId, String name, String email, String phone) {

        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
