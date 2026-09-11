package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request exactly duplicates an existing owner's identity — the same
 * {@code identityKey} (SHA-256 of normalizedTelephone + lowerEmail + soundex(lastName)).
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
    }
}
