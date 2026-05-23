package com.lmf.payment.paymentservice.infrastructure.persistence;

import com.lmf.payment.paymentservice.domain.Payment;
import com.lmf.payment.paymentservice.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.lmf.payment.paymentservice.infrastructure.persistence.repository.SpringDataPaymentRepository;
import com.lmf.payment.paymentservice.ports.output.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;

    @Override
    public void save(Payment payment) {

        springDataPaymentRepository.save(PaymentEntityMapper.toEntity(payment));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {

        return springDataPaymentRepository.findByOrderId(orderId).map(PaymentEntityMapper::toDomain);
    }
}
