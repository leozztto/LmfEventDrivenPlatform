package com.lmf.payment.paymentservice.application.service;

import com.lmf.payment.paymentservice.domain.exception.PaymentAlreadyProcessedException;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    private final PaymentRepository paymentRepository;

    public void validatePaymentDoesNotExist(UUID orderId) {

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {

            throw new PaymentAlreadyProcessedException(orderId);
        });
    }
}
