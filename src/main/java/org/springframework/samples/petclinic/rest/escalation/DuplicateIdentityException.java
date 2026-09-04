package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's whole identity key (telephone, email and household) is already
 * used by another owner. Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Identity already in use: " + identityKey);
    }
}
