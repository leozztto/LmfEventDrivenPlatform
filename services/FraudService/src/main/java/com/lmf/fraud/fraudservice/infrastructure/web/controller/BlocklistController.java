package com.lmf.fraud.fraudservice.infrastructure.web.controller;

import com.lmf.fraud.fraudservice.application.usecase.ManageBlocklistUseCase;
import com.lmf.fraud.fraudservice.infrastructure.web.request.CreateBlocklistEntryRequest;
import com.lmf.fraud.fraudservice.infrastructure.web.response.BlocklistEntryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Administração simples da lista de bloqueio de clientes — sem paginação nem autenticação, escopo
 * mínimo para a v1 das regras de fraude.
 */
@RestController
@RequestMapping("/api/v1/blocklist")
@RequiredArgsConstructor
public class BlocklistController {

    private final ManageBlocklistUseCase manageBlocklistUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlocklistEntryResponse create(@Valid @RequestBody CreateBlocklistEntryRequest request) {

        return BlocklistEntryResponse.from(
                manageBlocklistUseCase.create(request.customerId(), request.customerEmail(), request.reason()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {

        manageBlocklistUseCase.delete(id);
    }
}
