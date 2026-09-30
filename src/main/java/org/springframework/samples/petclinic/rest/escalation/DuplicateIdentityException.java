package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose whole derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}) already
 * belongs to another owner. This is the single duplicate check: the former separate
 * telephone, email and household checks are all expressed through this one key. Handled
 * globally by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with identityKey " + identityKey + " already exists");
        this.identityKey = identityKey;
    }

    /** The identityKey that is already in use. */
    public String getIdentityKey() {
        return this.identityKey;
    }
}
