package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted owner's whole identity key
 * ({@code normalizedTelephone + "|" + (email or empty) + "|" + (householdId or empty)})
 * equals an existing owner's. This is the single duplicate check: the former separate
 * telephone, email and household conflicts are all expressed through this one key.
 * Handled globally by {@link DuplicateIdentityExceptionHandler}, which responds 409
 * Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity key already exists (rejected value: " + identityKey + ")");
    }
}
