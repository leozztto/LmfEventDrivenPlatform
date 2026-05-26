package com.lmf.payment.paymentservice.infrastructure.persistence.repository;

import com.lmf.payment.paymentservice.domain.model.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.lmf.payment.paymentservice.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;

    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public Payment save(Payment payment) {

        return paymentEntityMapper.toDomain(springDataPaymentRepository.save(paymentEntityMapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {

        return springDataPaymentRepository.findByOrderId(orderId).map(paymentEntityMapper::toDomain);
    }
}
