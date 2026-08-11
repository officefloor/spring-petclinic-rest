package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request derives the same whole {@code identityKey} — the SHA-256 hex
 * over {@code normalizedTelephone|lowerEmail|soundex(lastName)} — as an existing owner. This single
 * rule now expresses what were previously the separate telephone and email duplicate checks.
 * Carries the colliding identityKey so the handler can report what collided.
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
