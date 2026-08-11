package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request derives the same whole {@code identityKey}
 * ({@code normalizedTelephone|email|householdId}) as an existing owner. This single rule
 * now expresses what were previously the separate telephone, email and household duplicate
 * checks. Carries the colliding identityKey so the handler can report what collided.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("An owner with the same identity already exists: '" + identityKey + "'");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
