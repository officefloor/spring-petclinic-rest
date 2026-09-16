package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Shared owner identity for the create-owner pipeline: derives the single canonical
 * {@code identityKey} that consolidates all duplicate detection.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId},
 * with telephone canonicalized to E.164 (see {@link OwnerTelephones}) and email lower-cased
 * (see {@link OwnerEmails}); a missing component contributes an empty string. Two owners are
 * duplicates exactly when their whole keys are equal — so members of the same household
 * (same {@code householdId}) with different telephones have different keys and are both
 * allowed.
 */
public final class OwnerIdentities {

    private OwnerIdentities() {
    }

    /**
     * Canonical identity key for the given components. {@code telephone} is normalized to
     * E.164 and {@code email} lower-cased before assembly; any {@code null} component
     * contributes an empty string.
     */
    public static String key(String telephone, String email, String householdId) {
        return part(OwnerTelephones.toE164(telephone)) + "|" + part(OwnerEmails.normalize(email))
                + "|" + part(householdId);
    }

    /** Canonical {@link #key(String, String, String)} for an existing owner's stored fields. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String part(String value) {
        return value == null ? "" : value;
    }
}
