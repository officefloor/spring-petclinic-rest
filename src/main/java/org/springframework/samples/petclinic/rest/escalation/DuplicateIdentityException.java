package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request has the same derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}) as an
 * existing owner — the single value all duplicate detection is now based on. Checked so it appears
 * in a {@code throws} clause and can be routed to its escalation handler, which responds 409
 * Conflict.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with this identity already exists: " + identityKey);
        this.identityKey = identityKey;
    }

    /** The identity key that is already in use. */
    public String getIdentityKey() {
        return this.identityKey;
    }
}
