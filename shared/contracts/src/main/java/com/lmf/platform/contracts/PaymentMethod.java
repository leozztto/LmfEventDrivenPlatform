package com.lmf.platform.contracts;

/**
 * Formas de pagamento aceitas pela plataforma. Não inclui adquirentes/provedores (ex.: MercadoPago) —
 * provedor é um conceito separado, definido no PaymentService.
 */
public enum PaymentMethod {

    CREDIT_CARD, DEBIT_CARD, PIX, BOLETO, PAYPAL, APPLE_PAY, GOOGLE_PAY
}
