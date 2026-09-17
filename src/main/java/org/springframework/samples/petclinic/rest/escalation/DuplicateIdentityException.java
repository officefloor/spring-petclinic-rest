package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's derived identity key — the canonical telephone, email and household
 * id joined by '|' — exactly equals an existing owner's. Carries the offending key so
 * {@link DuplicateIdentityExceptionHandler} can report it. Handled as a 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Another owner already has this identity: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
