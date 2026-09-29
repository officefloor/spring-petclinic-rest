package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireUniqueIdentity} when a create-owner request's whole derived
 * {@code identityKey} equals an existing owner's. Carries the offending key so
 * {@link DuplicateIdentityExceptionHandler} can report a 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity is already used by another owner: '" + identityKey + "'");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
