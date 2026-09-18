package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's identity — its normalized telephone, email and
 * household, captured as an {@code identityKey} — already belongs to another owner and the
 * request did not opt in with {@code sharesHousehold}. Handled by
 * {@link DuplicateIdentityExceptionHandler} as a 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
