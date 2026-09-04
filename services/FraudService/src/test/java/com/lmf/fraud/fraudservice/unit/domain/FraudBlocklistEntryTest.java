package com.lmf.fraud.fraudservice.unit.domain;

import com.lmf.fraud.fraudservice.domain.exception.InvalidBlocklistEntryException;
import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FraudBlocklistEntryTest {

    @Test
    void shouldCreateEntryWithCustomerIdOnly() {

        FraudBlocklistEntry entry = FraudBlocklistEntry.create(UUID.randomUUID(), null, "fraud confirmada");

        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getCustomerId()).isNotNull();
        assertThat(entry.getCustomerEmail()).isNull();
    }

    @Test
    void shouldCreateEntryWithCustomerEmailOnly() {

        FraudBlocklistEntry entry = FraudBlocklistEntry.create(null, "blocked@example.com", "chargeback");

        assertThat(entry.getCustomerEmail()).isEqualTo("blocked@example.com");
    }

    @Test
    void shouldThrowExceptionWhenNoIdentifierIsProvided() {

        assertThatThrownBy(() -> FraudBlocklistEntry.create(null, null, "sem identificador"))
                .isInstanceOf(InvalidBlocklistEntryException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {

        assertThatThrownBy(() -> FraudBlocklistEntry.create(null, "  ", "sem identificador"))
                .isInstanceOf(InvalidBlocklistEntryException.class);
    }
}
