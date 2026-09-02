package com.lmf.payment.paymentservice.infrastructure.gateway;

import com.lmf.payment.paymentservice.application.gateway.PaymentGateway;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayRequest;
import com.lmf.payment.paymentservice.application.gateway.dto.PaymentGatewayResponse;
import com.lmf.payment.paymentservice.domain.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gateway PIX simulado: aprova pagamentos à vista até R$ 20.000 e recusa acima disso.
 * PIX não admite parcelamento (a invariante já é garantida em {@link com.lmf.payment.paymentservice.domain.model.Payment}).
 */
@Slf4j
@Component
public class PixPaymentGateway implements PaymentGateway {

    private static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(20_000);

    @Override
    public PaymentGatewayResponse process(PaymentGatewayRequest paymentGatewayRequest) {

        log.info("Processing payment by PIX. paymentId={}, amount={}", paymentGatewayRequest.paymentId(), paymentGatewayRequest.amount());

        if (paymentGatewayRequest.amount().compareTo(MAX_AMOUNT) > 0) {

            return new PaymentGatewayResponse(false, null, "FAILED", "PIX amount above the allowed limit");
        }

        return new PaymentGatewayResponse(true, UUID.randomUUID().toString(), "APPROVED", null);
    }

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.PIX;
    }

    @Override
    public String provider() {
        return "PIX_PSP";
    }
}
