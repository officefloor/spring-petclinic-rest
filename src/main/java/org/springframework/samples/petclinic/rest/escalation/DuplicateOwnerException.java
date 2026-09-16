package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same derived {@code identityKey} as an
 * existing owner (see the create-owner pipeline's single duplicate check). Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey) {
        super("Owner already exists with identity: " + identityKey);
    }
}
