package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's derived identity key already belongs to another
 * owner. Handled by {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey) {
        super("An owner with identity key " + identityKey + " already exists");
    }
}
