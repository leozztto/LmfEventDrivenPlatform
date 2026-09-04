package com.lmf.fraud.fraudservice.domain.exception;

import java.util.UUID;

public class BlocklistEntryNotFoundException extends RuntimeException {

    public BlocklistEntryNotFoundException(UUID id) {
        super("Blocklist entry not found. id=" + id);
    }
}
