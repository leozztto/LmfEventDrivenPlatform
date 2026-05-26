package com.lmf.payment.paymentservice.domain.model;

import com.lmf.payment.paymentservice.domain.exception.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    private UUID id;

    private UUID orderId;

    private UUID customerId;

    private BigDecimal amount;

    private String currency;

    private PaymentMethod paymentMethod;

    private Integer installments;

    private PaymentStatus paymentStatus;

    private String provider;

    private String transactionId;

    private String gatewayStatus;

    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime failedAt;

    private OffsetDateTime updatedAt;

    private String failureReason;

    public static Payment create(UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, String provider) {

        Payment payment = Payment.builder().id(UUID.randomUUID()).orderId(Objects.requireNonNull(orderId, "OrderId is required")).customerId(Objects.requireNonNull(customerId, "CustomerId is required")).amount(amount).currency(normalizeCurrency(currency)).paymentMethod(paymentMethod).installments(resolveInstallments(installments)).paymentStatus(PaymentStatus.PENDING).provider(provider).gatewayStatus("PROCESSING").createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        payment.validate();

        return payment;
    }

    public static Payment restore(UUID id, UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, PaymentStatus paymentStatus, String provider, String transactionId, String gatewayStatus, OffsetDateTime createdAt, OffsetDateTime paidAt, OffsetDateTime failedAt, OffsetDateTime updatedAt, String failureReason) {

        return Payment.builder().id(id).orderId(orderId).customerId(customerId).amount(amount).currency(currency).paymentMethod(paymentMethod).installments(installments).paymentStatus(paymentStatus).provider(provider).transactionId(transactionId).gatewayStatus(gatewayStatus).createdAt(createdAt).paidAt(paidAt).failedAt(failedAt).updatedAt(updatedAt).failureReason(failureReason).build();
    }

    public void approve(String transactionId, String gatewayStatus) {

        ensurePendingPayment();

        this.paymentStatus = PaymentStatus.APPROVED;
        this.transactionId = transactionId;
        this.gatewayStatus = gatewayStatus;
        this.paidAt = OffsetDateTime.now();

        touch();
    }

    public void fail(String failureReason, String gatewayStatus) {

        ensurePendingPayment();

        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.gatewayStatus = gatewayStatus;
        this.failedAt = OffsetDateTime.now();

        touch();
    }

    public void cancel() {

        ensurePendingPayment();

        this.paymentStatus = PaymentStatus.CANCELLED;
        this.gatewayStatus = "CANCELLED";

        touch();
    }

    public boolean isApproved() {

        return this.paymentStatus == PaymentStatus.APPROVED;
    }

    public boolean isFailed() {

        return this.paymentStatus == PaymentStatus.FAILED;
    }

    public boolean isPending() {

        return this.paymentStatus == PaymentStatus.PENDING;
    }

    private void ensurePendingPayment() {

        if (!isPending()) {

            throw new InvalidPaymentStateException("Only pending payments can be processed");
        }
    }

    private void validate() {

        validateRequiredFields();
        validateAmount();
        validateCurrency();
        validateInstallments();
        validatePaymentMethodRules();
    }

    private void validateRequiredFields() {

        if (paymentMethod == null) {

            throw new InvalidPaymentMethodException("Payment method is required");
        }

        if (provider == null || provider.isBlank()) {

            throw new InvalidProviderException("Provider is required");
        }
    }

    private void validateAmount() {

        if (amount == null || amount.signum() <= 0) {

            throw new InvalidPaymentAmountException();
        }
    }

    private void validateCurrency() {

        if (currency == null || currency.isBlank()) {

            throw new InvalidCurrencyException("Currency is required");
        }
    }

    private void validateInstallments() {

        if (installments == null || installments < 1) {

            throw new InvalidInstallmentsException();
        }
    }

    private void validatePaymentMethodRules() {

        if (paymentMethod == PaymentMethod.PIX && installments > 1) {

            throw new InvalidPaymentMethodException("PIX payments cannot have installments");
        }
    }

    private void touch() {

        this.updatedAt = OffsetDateTime.now();
    }

    private static Integer resolveInstallments(Integer installments) {

        return installments == null ? 1 : installments;
    }

    private static String normalizeCurrency(String currency) {

        if (currency == null) {

            return null;
        }

        return currency.trim().toUpperCase();
    }
}