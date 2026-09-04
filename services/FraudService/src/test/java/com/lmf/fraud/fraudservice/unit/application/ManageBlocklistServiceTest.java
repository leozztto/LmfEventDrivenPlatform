package com.lmf.fraud.fraudservice.unit.application;

import com.lmf.fraud.fraudservice.application.service.ManageBlocklistService;
import com.lmf.fraud.fraudservice.domain.exception.BlocklistEntryNotFoundException;
import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import com.lmf.fraud.fraudservice.domain.repository.FraudBlocklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageBlocklistServiceTest {

    @Mock
    private FraudBlocklistRepository blocklistRepository;

    @InjectMocks
    private ManageBlocklistService manageBlocklistService;

    @Test
    void shouldCreateBlocklistEntry() {

        UUID customerId = UUID.randomUUID();

        when(blocklistRepository.save(any(FraudBlocklistEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudBlocklistEntry created = manageBlocklistService.create(customerId, null, "fraude confirmada");

        ArgumentCaptor<FraudBlocklistEntry> captor = ArgumentCaptor.forClass(FraudBlocklistEntry.class);
        verify(blocklistRepository).save(captor.capture());

        assertThat(captor.getValue().getCustomerId()).isEqualTo(customerId);
        assertThat(created.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void shouldDeleteExistingEntry() {

        UUID id = UUID.randomUUID();

        when(blocklistRepository.findById(id)).thenReturn(Optional.of(FraudBlocklistEntry.create(UUID.randomUUID(), null, "x")));

        manageBlocklistService.delete(id);

        verify(blocklistRepository).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeletingMissingEntry() {

        UUID id = UUID.randomUUID();

        when(blocklistRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manageBlocklistService.delete(id)).isInstanceOf(BlocklistEntryNotFoundException.class);

        verify(blocklistRepository, never()).deleteById(any());
    }
}
