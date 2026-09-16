package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request shares its identity key — normalized telephone, email and
 * surname Soundex — with an existing, active owner. Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(int existingOwnerId) {
        super("An owner with the same identity already exists (id " + existingOwnerId + ")");
    }
}
