package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's derived {@code identityKey} (SHA-256 hex over normalized
 * telephone, lower-cased email and soundex of the last name) exactly matches an existing owner's —
 * i.e. it is a full-key duplicate. Handled by {@link DuplicateIdentityExceptionHandler}, which responds
 * 409 (Conflict).
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity '" + identityKey + "' already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
