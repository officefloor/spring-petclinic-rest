package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's identity key (normalized telephone, email and
 * household id, joined by '|') is already used by another owner. Handled by
 * {@link DuplicateIdentityKeyExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityKeyException extends Exception {

    private final String identityKey;

    public DuplicateIdentityKeyException(String identityKey) {
        super("Identity key is already used by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
