package com.lmf.fraud.fraudservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmf.fraud.fraudservice.infrastructure.persistence.repository.SpringDataFraudBlocklistRepository;
import com.lmf.fraud.fraudservice.infrastructure.web.request.CreateBlocklistEntryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CRUD do endpoint de administração da blocklist contra o banco real. O seed do {@code V3} já
 * grava 2 registros no banco — os asserts abaixo nunca assumem a tabela vazia, sempre filtram pelo
 * id retornado pela própria chamada.
 */
class BlocklistPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataFraudBlocklistRepository springDataFraudBlocklistRepository;

    @Test
    void shouldCreateAndPersistBlocklistEntry() throws Exception {

        CreateBlocklistEntryRequest request = new CreateBlocklistEntryRequest(null, "new.blocked@example.com", "teste de integração");

        String response = mockMvc.perform(post("/api/v1/blocklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("new.blocked@example.com"))
                .andReturn().getResponse().getContentAsString();

        UUID createdId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        assertThat(springDataFraudBlocklistRepository.findById(createdId)).isPresent();
    }

    @Test
    void shouldDeleteBlocklistEntry() throws Exception {

        CreateBlocklistEntryRequest request = new CreateBlocklistEntryRequest(UUID.randomUUID(), null, "para remover");

        String response = mockMvc.perform(post("/api/v1/blocklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID createdId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(delete("/api/v1/blocklist/{id}", createdId)).andExpect(status().isNoContent());

        assertThat(springDataFraudBlocklistRepository.findById(createdId)).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownEntry() throws Exception {

        mockMvc.perform(delete("/api/v1/blocklist/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnBadRequestWhenNoIdentifierIsProvided() throws Exception {

        CreateBlocklistEntryRequest request = new CreateBlocklistEntryRequest(null, null, "sem identificador");

        mockMvc.perform(post("/api/v1/blocklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_BLOCKLIST_ENTRY"));
    }
}
