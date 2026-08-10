package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request produces an identityKey (normalized telephone {@code '|'} email
 * {@code '|'} householdId) that exactly matches an existing owner's. Consolidates the former
 * telephone, email and household conflict checks. Handled globally by
 * {@link OwnerIdentityConflictExceptionHandler}, which responds 409.
 */
public class OwnerIdentityConflictException extends Exception {

    public OwnerIdentityConflictException(String identityKey) {
        super("Owner already exists with identityKey: " + identityKey);
    }
}
