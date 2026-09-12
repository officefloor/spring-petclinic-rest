package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's {@code identityKey} — the SHA-256 over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} — equals that of
 * an existing, non-deleted owner. Handled by {@link OwnerIdentityConflictExceptionHandler},
 * which responds 409 Conflict.
 */
public class OwnerIdentityConflictException extends Exception {

    private final String identityKey;

    public OwnerIdentityConflictException(String identityKey) {
        super("Another owner already exists with the same identity key: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
