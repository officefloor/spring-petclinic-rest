package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when an existing owner already has the request's identity key
 * (same normalized telephone, lower-cased email and {@code soundex(lastName)}). Carries the
 * offending identity key so {@link DuplicateOwnerIdentityExceptionHandler} can report it. Handled as
 * 409 Conflict.
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
