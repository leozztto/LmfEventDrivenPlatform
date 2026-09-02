package com.lmf.payment.paymentservice.infrastructure.gateway;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway de boleto simulado: o boleto é sempre emitido com sucesso (a confirmação real do pagamento
 * chegaria depois, de forma assíncrona, num fluxo fora do escopo desta simulação).
 */
@Slf4j
@Component
public class TicketPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        log.info("Issuing bank slip (boleto). paymentId={}, amount={}", paymentGatewayRequest.paymentId(), paymentGatewayRequest.amount());

        return new PaymentGatewayResponse(true, UUID.randomUUID().toString(), "APPROVED", null);
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.BOLETO;
    }

    @Override
    public String provider() {
        return "BANK_SLIP";
    }
}
