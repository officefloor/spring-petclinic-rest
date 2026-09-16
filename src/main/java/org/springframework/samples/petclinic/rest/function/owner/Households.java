package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Derives the household an owner belongs to. A household is keyed on
 * {@code (lastName, postcode)}: its {@code householdId} is the first 12 hex characters of
 * SHA-256 over {@code normalizedLastName + '|' + postcode}, so two owners with the same
 * last name and postcode deterministically resolve to the same id without any explicit
 * linking. Used to detect household duplicates ({@link EnsureUniqueIdentity}), to stamp
 * each owner with its id ({@link AssignHousehold}) and to size the household
 * ({@link AssignHouseholdSize}).
 */
final class Households {

    private Households() {
    }

    /**
     * The household id for the given last name and postcode: the first 12 hex characters of
     * SHA-256 over the fixed version-2 tag, the normalized last name and the postcode (see
     * {@link IdentityVersion}). Returns null when the postcode is blank, since without a
     * postcode there is no household key.
     */
    static String householdId(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        return Sha256.hex(IdentityVersion.TAG + '|' + normalizeName(lastName) + '|' + postcode).substring(0, 12);
    }

    /** How many existing owners already carry the given household id (0 when it is null). */
    static int memberCount(OwnerRepository repository, String householdId) {
        if (householdId == null) {
            return 0;
        }
        int count = 0;
        for (Owner owner : repository.findAll()) {
            if (householdId.equals(owner.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The highest membership level among the existing owners already carrying the given
     * household id, or empty when the household has no existing members (id null or
     * unmatched). Used to cap a new member's level ({@link AssignMembershipLevelCap}).
     */
    static OptionalInt maxMemberLevel(OwnerRepository repository, String householdId) {
        if (householdId == null) {
            return OptionalInt.empty();
        }
        int max = Integer.MIN_VALUE;
        for (Owner owner : repository.findAll()) {
            if (householdId.equals(owner.getHouseholdId())) {
                max = Math.max(max, MembershipLevel.of(owner));
            }
        }
        return max == Integer.MIN_VALUE ? OptionalInt.empty() : OptionalInt.of(max);
    }

    /** Case-fold and collapse whitespace so trivial spacing/casing differences still match. */
    private static String normalizeName(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
