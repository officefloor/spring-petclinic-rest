package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request derives an identity key that is already used by another
 * owner. The key consolidates telephone, email and household, so this is the single duplicate
 * signal for owner creation. Handled by {@link DuplicateIdentityExceptionHandler}, which
 * responds 409 with the conflicting key.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity is already used by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
