package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join an existing household — its deterministic
 * {@code householdId} (derived from the normalized lastName and postcode) equals an existing owner's —
 * without declaring {@code sharesHousehold}. Handled globally by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner already exists in the same household: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
