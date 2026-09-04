package com.lmf.payment.paymentservice.domain.exception;

public class InvalidInstallmentsException extends BusinessException {

    public InvalidInstallmentsException() {
        super("Installments must be greater than zero");
    }
}
