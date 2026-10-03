package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the new owner's identity key collides with an existing,
 * non-deleted owner's — i.e. they share the same normalized telephone, lower-cased email
 * and Soundex last name. Handled globally by {@link DuplicateOwnerExceptionHandler}, which
 * responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey, Integer existingOwnerId) {
        super("Owner with identity key '" + identityKey + "' already exists (owner " + existingOwnerId + ")");
    }
}
