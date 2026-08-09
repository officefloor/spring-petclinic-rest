package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose derived identity key — {@code normalizedTelephone + '|' +
 * (email or empty) + '|' + householdId} — exactly matches an existing owner's. This single key
 * consolidates the former separate telephone, email and household duplicate checks. Handled globally
 * by {@link DuplicateIdentityExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identity key '" + identityKey + "' already exists");
    }
}
