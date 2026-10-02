package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner has the same derived {@code identityKey} (normalized
 * telephone, email and household id together) as an existing owner. Carries the offending key so
 * {@link DuplicateIdentityExceptionHandler} can report it. Checked so it appears in the function's
 * {@code throws} clause and is routed as an escalation (a 409 Conflict).
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
