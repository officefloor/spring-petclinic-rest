package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose derived {@code identityKey} exactly equals an
 * existing owner's. This is the single, consolidated duplicate check (telephone,
 * email and household combined into one key). Handled globally by
 * {@link DuplicateOwnerIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String message) {
        super(message);
    }
}
