package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request is a hard duplicate: an existing active owner already has the same
 * identity key (SHA-256 over the canonical telephone, email and Soundex of the last name), and the
 * request did not set {@code sharesHousehold}. Carries the offending identity key so
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
