package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Hashes;

/**
 * The single definition of an owner's {@code householdId}: a deterministic identifier derived from
 * the owner's last name and postcode, so owners with the same last name and postcode share it
 * automatically. The value is the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode}, where the last name is compared case-insensitively
 * after collapsing runs of whitespace.
 *
 * <p>Because it is a pure function of {@code (lastName, postcode)}, every owner computes its own id
 * independently and members of one household always agree — there is nothing to link or backfill.
 * Shared by {@link AssignHousehold} (which stamps it onto the new owner), {@link RequireUniqueIdentity}
 * (duplicate detection) and {@link AssignOwnerPossibleDuplicate} (soft-match), so every place that
 * keys off the household applies the one rule and they cannot drift apart.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class HouseholdKey {

    /** Length of the household id: the first 12 hex characters of the SHA-256 digest. */
    private static final int ID_LENGTH = 12;

    private HouseholdKey() {
    }

    /**
     * The deterministic {@code householdId} for the given last name and postcode: the first 12 hex
     * characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. A {@code null} postcode
     * is treated as the empty string.
     */
    static String householdId(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return Hashes.sha256Hex(key).substring(0, ID_LENGTH);
    }

    /**
     * The deterministic {@code householdId} of the given owner — the single home of "which two fields
     * make a household", mirroring {@link IdentityKey#ofOwner}, so callers never re-pluck the tuple.
     */
    static String householdId(Owner owner) {
        return householdId(owner.getLastName(), owner.getPostcode());
    }

    /** Lower-cases and collapses runs of whitespace to a single space for case- and
     *  whitespace-insensitive comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
