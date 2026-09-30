package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Hashing;
import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of household identity, used by {@link AssignOwnerHousehold} (which
 * stamps the householdId) and by the household-keyed rules (household size, membership-level
 * ceiling), so the rule lives in one place. Duplicate detection is keyed on the identityKey,
 * not the householdId.
 *
 * <p>A household is keyed on {@code (lastName, postcode)}: two owners are in the same
 * household when their normalized lastName and postcode match. The householdId is therefore
 * <em>deterministic</em> — the first 12 hex characters of SHA-256 over
 * {@code V2 + '|' + normalizedLastName + '|' + postcode}, the fixed version-2 tag mixed in — so
 * owners with the same lastName and postcode share it automatically, with no lookup. lastName is
 * normalized case-insensitively with collapsed whitespace; the postcode is used as given (empty
 * when absent).
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * The deterministic householdId for the given lastName and postcode: the first 12 hex
     * characters of SHA-256 over {@code V2 + '|' + normalizedLastName + '|' + postcode}. Owners
     * with the same lastName and postcode therefore compute the same householdId automatically.
     */
    static String householdId(String lastName, String postcode) {
        // Version 2: mix in the fixed version tag so the householdId is rederived and no value
        // produced under version 1 is produced again.
        String input = IdentityVersion.TAG + "|" + normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return Hashing.sha256Hex(input).substring(0, 12);
    }

    /**
     * The number of owners in the given household — those whose {@code householdId} matches.
     * A {@code null} householdId matches nothing. Used by the household-keyed rules so the
     * membership comparison lives in the same place as the identity that defines it.
     */
    static int memberCount(String householdId, Iterable<Owner> owners) {
        if (householdId == null) {
            return 0;
        }
        int count = 0;
        for (Owner owner : owners) {
            if (householdId.equals(owner.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The highest {@code membershipLevel} among the existing owners in the given household —
     * those whose {@code householdId} matches — or {@code null} when the household has no
     * existing member with a level. A {@code null} householdId matches nothing. Used by the
     * level ceiling so a new owner cannot leapfrog their household's current maximum.
     */
    static Integer maxMembershipLevel(String householdId, Iterable<Owner> owners) {
        if (householdId == null) {
            return null;
        }
        Integer max = null;
        for (Owner owner : owners) {
            if (householdId.equals(owner.getHouseholdId())) {
                Integer level = owner.getMembershipLevel();
                if (level != null && (max == null || level > max)) {
                    max = level;
                }
            }
        }
        return max;
    }

    /** Lower-case and collapse whitespace runs to a single space for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
