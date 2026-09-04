package com.lmf.fraud.fraudservice.domain.exception;

public class InvalidBlocklistEntryException extends RuntimeException {

    public InvalidBlocklistEntryException() {
        super("A blocklist entry must have at least a customerId or a customerEmail");
    }
}
