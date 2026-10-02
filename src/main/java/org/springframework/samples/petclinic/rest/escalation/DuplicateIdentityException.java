package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner resolves to the same identity key (the SHA-256 hex over
 * {@code normalizedTelephone | lowerEmail | soundex(lastName)}) as an existing, non-deleted owner — i.e.
 * the two owners are the same identity. Carries the offending identity key so
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
