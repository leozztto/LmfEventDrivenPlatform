package com.lmf.payment.paymentservice.domain.model;

import com.lmf.payment.paymentservice.domain.exception.BusinessException;
import com.lmf.payment.paymentservice.domain.exception.InvalidInstallmentsException;
import com.lmf.payment.paymentservice.domain.exception.InvalidPaymentAmountException;
import com.lmf.payment.paymentservice.domain.exception.InvalidPaymentMethodException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

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

    private Payment() {
    }

    public static Payment create(UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, String provider) {

        Payment payment = new Payment();

        payment.id = UUID.randomUUID();
        payment.orderId = Objects.requireNonNull(orderId);
        payment.customerId = Objects.requireNonNull(customerId);
        payment.amount = amount;
        payment.currency = currency;
        payment.paymentMethod = paymentMethod;
        payment.installments = installments == null ? 1 : installments;
        payment.paymentStatus = PaymentStatus.PENDING;
        payment.provider = provider;
        payment.gatewayStatus = "PROCESSING";
        payment.createdAt = OffsetDateTime.now();
        payment.updatedAt = OffsetDateTime.now();

        payment.validate();

        return payment;
    }

    public static Payment restore(UUID id, UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod paymentMethod, Integer installments, PaymentStatus paymentStatus, String provider, String transactionId, String gatewayStatus, OffsetDateTime createdAt, OffsetDateTime paidAt, OffsetDateTime failedAt, OffsetDateTime updatedAt, String failureReason) {

        Payment payment = new Payment();

        payment.id = id;
        payment.orderId = orderId;
        payment.customerId = customerId;
        payment.amount = amount;
        payment.currency = currency;
        payment.paymentMethod = paymentMethod;
        payment.installments = installments;
        payment.paymentStatus = paymentStatus;
        payment.provider = provider;
        payment.transactionId = transactionId;
        payment.gatewayStatus = gatewayStatus;
        payment.createdAt = createdAt;
        payment.paidAt = paidAt;
        payment.failedAt = failedAt;
        payment.updatedAt = updatedAt;
        payment.failureReason = failureReason;

        return payment;
    }

    public void approve(String transactionId) {

        ensurePendingPayment();

        this.paymentStatus = PaymentStatus.APPROVED;
        this.transactionId = transactionId;
        this.gatewayStatus = "APPROVED";
        this.paidAt = OffsetDateTime.now();
        touch();
    }

    public void fail(String failureReason) {

        ensurePendingPayment();

        this.paymentStatus = PaymentStatus.FAILED;
        this.gatewayStatus = "FAILED";
        this.failureReason = failureReason;
        this.failedAt = OffsetDateTime.now();
        touch();
    }

    public void cancel() {

        if (this.paymentStatus != PaymentStatus.PENDING) {

            throw new BusinessException("Only pending payments can be cancelled");
        }

        this.paymentStatus = PaymentStatus.CANCELLED;
        this.gatewayStatus = "CANCELLED";
        touch();
    }

    private void ensurePendingPayment() {

        if (this.paymentStatus != PaymentStatus.PENDING) {

            throw new BusinessException("Only pending payments can be processed");
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

            throw new BusinessException("Provider is required");
        }
    }

    private void validateAmount() {

        if (amount == null || amount.signum() <= 0) {

            throw new InvalidPaymentAmountException();
        }
    }

    private void validateCurrency() {

        if (currency == null || currency.isBlank()) {

            throw new BusinessException("Currency is required");
        }
    }

    private void validateInstallments() {

        if (installments != null && installments < 1) {

            throw new InvalidInstallmentsException();
        }
    }

    private void validatePaymentMethodRules() {

        if (paymentMethod == PaymentMethod.PIX && installments != null && installments > 1) {

            throw new InvalidPaymentMethodException("PIX payments cannot have installments");
        }
    }

    private void touch() {

        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Integer getInstallments() {
        return installments;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getProvider() {
        return provider;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getFailedAt() {
        return failedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}