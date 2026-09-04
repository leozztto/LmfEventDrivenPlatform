package com.lmf.fraud.fraudservice.unit.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.fraud.fraudservice.application.usecase.ManageBlocklistUseCase;
import com.lmf.fraud.fraudservice.domain.exception.BlocklistEntryNotFoundException;
import com.lmf.fraud.fraudservice.domain.model.FraudBlocklistEntry;
import com.lmf.fraud.fraudservice.infrastructure.web.controller.BlocklistController;
import com.lmf.fraud.fraudservice.infrastructure.web.request.CreateBlocklistEntryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlocklistController.class)
class BlocklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageBlocklistUseCase manageBlocklistUseCase;

    @Test
    @DisplayName("Should create a blocklist entry successfully")
    void shouldCreateBlocklistEntrySuccessfully() throws Exception {

        UUID customerId = UUID.randomUUID();

        CreateBlocklistEntryRequest request = new CreateBlocklistEntryRequest(customerId, null, "fraude confirmada");

        FraudBlocklistEntry created = FraudBlocklistEntry.create(customerId, null, "fraude confirmada");

        when(manageBlocklistUseCase.create(customerId, null, "fraude confirmada")).thenReturn(created);

        mockMvc.perform(post("/api/v1/blocklist").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.reason").value("fraude confirmada"));
    }

    @Test
    @DisplayName("Should return bad request when reason is blank")
    void shouldReturnBadRequestWhenReasonIsBlank() throws Exception {

        String invalidRequest = """
                {
                  "customerEmail": "blocked@example.com",
                  "reason": ""
                }
                """;

        mockMvc.perform(post("/api/v1/blocklist").contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should delete an existing entry")
    void shouldDeleteExistingEntry() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/blocklist/{id}", id)).andExpect(status().isNoContent());

        verify(manageBlocklistUseCase).delete(id);
    }

    @Test
    @DisplayName("Should return 404 when deleting a missing entry")
    void shouldReturnNotFoundWhenDeletingMissingEntry() throws Exception {

        UUID id = UUID.randomUUID();

        doThrow(new BlocklistEntryNotFoundException(id)).when(manageBlocklistUseCase).delete(id);

        mockMvc.perform(delete("/api/v1/blocklist/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
