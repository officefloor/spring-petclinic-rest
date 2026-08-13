package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when a new owner's whole identity key (normalized telephone, email and
 * householdId) is already used by another owner. Handled by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
