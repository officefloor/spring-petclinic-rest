package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's derived identity key (its normalized
 * telephone, email and household id) exactly matches an existing owner's. Carries the offending
 * identity key so {@link DuplicateOwnerIdentityExceptionHandler} can report it. Handled as 409
 * Conflict.
 */
public class DuplicateOwnerIdentityException extends Exception {

    private final String identityKey;

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
