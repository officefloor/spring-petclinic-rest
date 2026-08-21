package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request derives an {@code identityKey} - the consolidated
 * {@code normalizedTelephone|email|householdId} value - that exactly equals an existing owner's.
 * This one key expresses what used to be the separate telephone, email and household duplicate
 * checks. Handled globally by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
