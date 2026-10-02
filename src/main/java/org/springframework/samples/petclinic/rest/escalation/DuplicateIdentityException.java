package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when a new owner would be an identity duplicate: an existing
 * active owner already carries the identical identity key (SHA-256 over the normalized telephone,
 * lower-cased email and soundex of the last name).
 *
 * <p>Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
