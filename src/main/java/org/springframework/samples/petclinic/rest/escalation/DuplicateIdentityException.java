package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same derived identity key (normalized
 * telephone, email and household id) as an existing owner, i.e. a whole-key match.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
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
