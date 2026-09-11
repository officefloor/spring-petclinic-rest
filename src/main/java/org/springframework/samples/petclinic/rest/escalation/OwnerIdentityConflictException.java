package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would add an owner whose whole {@code identityKey}
 * (normalized telephone, email and household) equals that of an existing owner. Handled
 * by {@link OwnerIdentityConflictExceptionHandler}, which responds 409 Conflict.
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
