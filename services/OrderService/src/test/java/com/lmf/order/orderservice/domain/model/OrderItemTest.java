package com.lmf.order.orderservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void shouldCalculateSubtotalCorrectly() {

        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                2,
                BigDecimal.valueOf(100)
        );

        assertEquals(
                BigDecimal.valueOf(200),
                item.getSubtotal()
        );
    }

    @Test
    @DisplayName("Should throw exception when quantity is invalid")
    void shouldThrowExceptionWhenQuantityIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        UUID.randomUUID(),
                        0,
                        BigDecimal.valueOf(100)
                )
        );
    }

    @Test
    @DisplayName("Should throw exception when price is invalid")
    void shouldThrowExceptionWhenPriceIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        UUID.randomUUID(),
                        1,
                        BigDecimal.ZERO
                )
        );
    }
}
