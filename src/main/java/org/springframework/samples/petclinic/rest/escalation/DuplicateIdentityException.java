package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueIdentity} when a create-owner request's whole derived
 * {@code identityKey} (the SHA-256 hex of {@code normalizedTelephone|lowerEmail|soundex(lastName)})
 * equals an existing owner's. This single key consolidates the former separate telephone, email and
 * household duplicate checks. Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already used by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
