package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's derived {@code identityKey} (normalizedTelephone + '|' +
 * (email or empty) + '|' + householdId) exactly equals an existing owner's identityKey. This one key
 * subsumes the former separate telephone, email and household duplicate checks. Handled globally by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity already exists: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
