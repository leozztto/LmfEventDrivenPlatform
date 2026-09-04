package com.lmf.fraud.fraudservice.application.usecase;

import com.lmf.platform.contracts.OrderCreatedEvent;

public interface EvaluateFraudUseCase {

    void execute(OrderCreatedEvent event);
}
