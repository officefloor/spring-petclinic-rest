package org.springframework.samples.petclinic.util;

/**
 * Thrown when a new owner's identity key matches an existing, non-deleted owner. Handled by the
 * REST layer as a 409 Conflict.
 */
public class DuplicateOwnerException extends RuntimeException {

    public DuplicateOwnerException() {
        super("An owner with the same identity already exists");
    }
}
