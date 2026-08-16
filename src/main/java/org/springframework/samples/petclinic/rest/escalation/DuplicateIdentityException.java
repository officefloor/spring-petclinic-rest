package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckIdentityUnique}
 * when a new owner's derived {@code identityKey} exactly equals an existing owner's, i.e. the owner
 * is a duplicate. Consolidates the former telephone/email/household duplicate exceptions into one.
 * Handled globally by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with this identity already exists: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
