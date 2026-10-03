package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.OptionalInt;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared household rules for the owner-create pipeline. A household is now keyed on
 * {@code (lastName, postcode)}: every owner with a postcode derives a deterministic
 * {@link Id} from its normalized last name and postcode, so owners with the same last name
 * and postcode share the same id automatically, regardless of creation order. The id is
 * defined once here so the resolver ({@link ResolveHousehold}) and the new owner
 * ({@link BuildOwner}) agree on it; it groups owners for the household-size and membership-level
 * rules but no longer gates duplicate detection, which is now the single owner identity key
 * ({@link OwnerIdentity}).
 */
final class Household {

    private Household() {
    }

    /**
     * The household identifier published between steps. A dedicated type keeps the variable
     * unambiguous (OfficeFloor matches variables by type) and distinct from other strings in
     * the pipeline. {@link #ABSENT} (an empty value) means the new owner has no postcode and
     * therefore no household.
     */
    record Id(String value) {

        static final Id ABSENT = new Id("");

        boolean isPresent() {
            return this.value != null && !this.value.isBlank();
        }
    }

    /**
     * Stable identifier for the request's household: the first 12 upper-case hex characters of
     * the SHA-256 of {@code normalizedLastName + "|" + postcode + "|" + IDENTITY_VERSION}, so every
     * owner with the same last name and postcode derives the same value regardless of creation order.
     * The {@link Owner#IDENTITY_VERSION version-2 tag} is mixed into the seed, so every household id
     * differs from the value it carried under version 1. {@link Id#ABSENT} when the request carries
     * no postcode (no postcode means no household).
     */
    static Id idFor(OwnerFieldsDto request) {
        String postcode = normalizePostcode(request.getPostcode());
        if (postcode == null) {
            return Id.ABSENT;
        }
        String seed = normalizeName(request.getLastName()) + "|" + postcode + "|" + Owner.IDENTITY_VERSION;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return new Id(sb.toString());
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * The number of owners carrying {@code householdId}. The single household-size lookup, reused
     * wherever the membership points rule needs a household's member count. {@code 0} for a
     * {@code null}/blank id (an owner with no postcode has no household). Scans only
     * {@link ActiveOwners active owners}, so a soft-deleted owner does not count towards a
     * household's size.
     */
    static int size(String householdId, OwnerRepository ownerRepository) {
        if (householdId == null || householdId.isBlank()) {
            return 0;
        }
        int count = 0;
        for (Owner existing : ActiveOwners.list(ownerRepository)) {
            if (householdId.equals(existing.getHouseholdId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * The maximum {@link Owner#getMembershipLevel() membershipLevel} among the members of
     * {@code householdId} other than {@code exclude}, or {@link OptionalInt#empty()} when the
     * household has no such member (so no cap applies). Each member's household size is resolved
     * first — exactly as {@link LoadOwner} does on read — so its level is scored on a fully
     * populated household rather than as a lone member. Scans only {@link ActiveOwners active
     * owners}, consistent with {@link #size}, so a soft-deleted owner does not raise the ceiling.
     * {@link OptionalInt#empty()} for a {@code null}/blank id.
     */
    static OptionalInt maxMemberLevel(String householdId, Owner exclude, OwnerRepository ownerRepository) {
        if (householdId == null || householdId.isBlank()) {
            return OptionalInt.empty();
        }
        int max = -1;
        for (Owner existing : ActiveOwners.list(ownerRepository)) {
            if (exclude != null && exclude.getId() != null && exclude.getId().equals(existing.getId())) {
                continue;
            }
            if (householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdSize(Math.max(1, size(householdId, ownerRepository)));
                max = Math.max(max, existing.getMembershipLevel());
            }
        }
        return max < 0 ? OptionalInt.empty() : OptionalInt.of(max);
    }

    /**
     * Normalize a last name for the household key: trim, collapse any run of whitespace to a
     * single space, and lower-case. Yields {@code ""} for a {@code null} input.
     */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Trimmed postcode, or {@code null} when absent/blank (no postcode means no household). */
    private static String normalizePostcode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
