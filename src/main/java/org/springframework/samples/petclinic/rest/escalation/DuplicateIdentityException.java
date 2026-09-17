package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's whole identity key — its normalized telephone,
 * email and household id together — already belongs to another owner. Handled by
 * {@link DuplicateIdentityExceptionHandler} as a 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
