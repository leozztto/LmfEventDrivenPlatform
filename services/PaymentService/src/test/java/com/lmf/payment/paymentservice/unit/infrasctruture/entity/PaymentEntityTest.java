package com.lmf.payment.paymentservice.unit.infrasctruture.entity;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import com.lmf.payment.paymentservice.domain.model.PaymentStatus;
import com.lmf.payment.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentEntityTest {

    @Test
    @DisplayName("Should create payment entity with all fields")
    void shouldCreatePaymentEntityWithAllFields() {

        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PaymentEntity paymentEntity = new PaymentEntity(id, orderId, customerId, new BigDecimal("299.90"), "BRL", PaymentMethod.CREDIT_CARD, 3, PaymentStatus.APPROVED, "MERCADO_PAGO", "txn-123", "APPROVED", now, now, null, now, null);

        assertNotNull(paymentEntity);

        assertEquals(id, paymentEntity.getId());
        assertEquals(orderId, paymentEntity.getOrderId());
        assertEquals(customerId, paymentEntity.getCustomerId());

        assertEquals(new BigDecimal("299.90"), paymentEntity.getAmount());
        assertEquals("BRL", paymentEntity.getCurrency());

        assertEquals(PaymentMethod.CREDIT_CARD, paymentEntity.getPaymentMethod());
        assertEquals(3, paymentEntity.getInstallments());

        assertEquals(PaymentStatus.APPROVED, paymentEntity.getPaymentStatus());

        assertEquals("MERCADO_PAGO", paymentEntity.getProvider());
        assertEquals("txn-123", paymentEntity.getTransactionId());
        assertEquals("APPROVED", paymentEntity.getGatewayStatus());

        assertEquals(now, paymentEntity.getCreatedAt());
        assertEquals(now, paymentEntity.getPaidAt());
        assertEquals(null, paymentEntity.getFailedAt());
        assertEquals(now, paymentEntity.getUpdatedAt());

        assertEquals(null, paymentEntity.getFailureReason());
    }

    @Test
    @DisplayName("Should create failed payment entity")
    void shouldCreateFailedPaymentEntity() {

        UUID id = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PaymentEntity paymentEntity = new PaymentEntity(id, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("150.00"), "BRL", PaymentMethod.PIX, 1, PaymentStatus.FAILED, "PAGSEGURO", null, "DECLINED", now, null, now, now, "Insufficient funds");

        assertEquals(PaymentStatus.FAILED, paymentEntity.getPaymentStatus());
        assertEquals("DECLINED", paymentEntity.getGatewayStatus());
        assertEquals("Insufficient funds", paymentEntity.getFailureReason());
        assertEquals(now, paymentEntity.getFailedAt());
    }
}