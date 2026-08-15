package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's derived identityKey (normalized telephone, email and
 * householdId) exactly matches an existing owner's, i.e. the whole key collides. Handled
 * globally by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identityKey already exists: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
