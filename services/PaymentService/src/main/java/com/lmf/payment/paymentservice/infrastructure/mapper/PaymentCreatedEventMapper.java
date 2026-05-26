package com.lmf.payment.paymentservice.infrastructure.mapper;

import com.lmf.payment.paymentservice.application.event.PaymentCreatedEvent;
import com.lmf.payment.paymentservice.domain.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentCreatedEventMapper {

    PaymentCreatedEvent toEvent(Payment payment);
}
