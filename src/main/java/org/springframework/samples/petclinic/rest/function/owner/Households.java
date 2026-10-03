package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for what a "household" is: two owners share one when their last
 * name and postcode match. The household is keyed on {@code (lastName, postcode)} — the
 * last name is trimmed, has runs of whitespace collapsed to a single space and is
 * lower-cased, and the postcode is used verbatim — so the {@code householdId} is fully
 * deterministic: owners with the same last name and postcode compute the same value
 * independently, with no explicit linking.
 *
 * <p>The {@code householdId} is used both when {@link AssignHousehold} stamps it onto a
 * new owner and when {@link #members} selects an owner's existing household members, so
 * the two cannot drift apart. An owner with no postcode belongs to no household and gets
 * a null id.
 */
final class Households {

    private Households() {
    }

    /** Lower-cases and collapses runs of whitespace to a single space so the comparison
     *  ignores case and incidental spacing differences. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** The {@code householdId} for the household an owner with this raw last name and
     *  postcode belongs to, or {@code null} when the postcode is absent (no household). */
    static String householdId(String rawLastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        return deriveHouseholdId(normalize(rawLastName), postcode);
    }

    /**
     * The current members of the household identified by {@code householdId}: the
     * persisted, non-soft-deleted owners whose own last name and postcode resolve to the
     * same household id. Each owner's id is recomputed from its own fields (not read from a
     * stored column), so this selection and {@link AssignHousehold}'s stamp cannot drift
     * apart. A {@code null} {@code householdId} — an owner with no postcode belongs to no
     * household — selects nobody, and an owner not yet persisted (null id, e.g. the owner
     * being created) is never included. This is the single definition of "an owner's
     * household members"; callers supply only their own aggregation over the result.
     */
    static List<Owner> members(String householdId, Iterable<Owner> owners) {
        List<Owner> members = new ArrayList<>();
        if (householdId == null) {
            return members;
        }
        for (Owner owner : owners) {
            if (owner.getId() == null) {
                continue;
            }
            // A soft-deleted owner no longer occupies its household, so it is not a member.
            if (Boolean.TRUE.equals(owner.getDeleted())) {
                continue;
            }
            if (householdId.equals(householdId(owner.getLastName(), owner.getPostcode()))) {
                members.add(owner);
            }
        }
        return members;
    }

    /** Stable identifier for a household: the first 12 upper-case hex characters of the
     *  SHA-256 of {@code normalizedLastName + "|" + postcode}. */
    static String deriveHouseholdId(String normalizedLastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
