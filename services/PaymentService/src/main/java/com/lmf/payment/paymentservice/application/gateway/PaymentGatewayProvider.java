package com.lmf.payment.paymentservice.application.gateway;

import com.lmf.payment.paymentservice.domain.model.PaymentMethod;

/**
 * Porta para obter o {@link PaymentGateway} adequado a uma forma de pagamento. A implementação
 * (que conhece os adaptadores concretos) vive na camada de infraestrutura.
 */
public interface PaymentGatewayProvider {

    PaymentGateway resolve(PaymentMethod paymentMethod);
}
