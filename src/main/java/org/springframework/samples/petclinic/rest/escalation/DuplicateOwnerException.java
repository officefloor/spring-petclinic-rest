package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same derived {@code identityKey}
 * ({@code normalizedTelephone|email|householdId}) as an existing owner. Consolidates the former
 * separate telephone, email and household duplicate checks into one. Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String identityKey) {
        super("An owner with the same identityKey already exists: " + identityKey);
    }
}
