package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's derived {@code identityKey} exactly
 * matches an existing owner's — the single duplicate-detection rule that consolidates the former
 * telephone, email and household checks. Handled globally by {@link
 * DuplicateOwnerIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String identityKey) {
        super("An owner with identityKey " + identityKey + " already exists");
    }
}
