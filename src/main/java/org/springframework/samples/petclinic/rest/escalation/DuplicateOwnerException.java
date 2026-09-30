package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose <em>identity</em> — the
 * {@code normalizedTelephone|email|householdId} composite (see
 * {@code org.springframework.samples.petclinic.model.OwnerIdentity}) — already belongs to
 * another owner. Owners sharing only a household (same lastName and postcode) but differing in
 * telephone or email are <em>not</em> duplicates and may coexist as household members; only a
 * full identity match is rejected. Setting {@code sharesHousehold} bypasses this check (the new
 * owner is created as a declared household member). Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    private final String identityKey;

    public DuplicateOwnerException(String identityKey) {
        super("An owner with the same identity already exists");
        this.identityKey = identityKey;
    }

    /** The identityKey that is already in use. */
    public String getIdentityKey() {
        return this.identityKey;
    }
}
