package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when an existing owner already belongs to the request's
 * household (same lastName and postcode) and the request did not declare itself a member with
 * {@code sharesHousehold}. Carries the offending household id so
 * {@link DuplicateOwnerIdentityExceptionHandler} can report it. Handled as 409 Conflict.
 */
public class DuplicateOwnerIdentityException extends Exception {

    private final String identityKey;

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
