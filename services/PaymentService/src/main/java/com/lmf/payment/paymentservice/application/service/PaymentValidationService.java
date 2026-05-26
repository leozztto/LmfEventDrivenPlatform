package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.domain.exception.PaymentAlreadyProcessedException;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    private final PaymentRepository paymentRepository;

    public void validatePaymentDoesNotExist(UUID orderId) {

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {

            log.warn("Payment already exists for orderId={}", orderId);

            throw new PaymentAlreadyProcessedException(orderId);
        });
    }
}
