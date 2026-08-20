package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose whole derived {@code identityKey} (SHA-256 over normalized
 * telephone, lower-cased email and the Soundex of the last name) equals an existing owner's. This
 * single check consolidates what were once separate telephone, email and household duplicate checks.
 * Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey) {
        super("An owner with identityKey " + identityKey + " already exists");
    }
}
