package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Single source of truth for what a "household" is: two owners share one when their last
 * name and postcode match. The household is keyed on {@code (lastName, postcode)} — the
 * last name is trimmed, has runs of whitespace collapsed to a single space and is
 * lower-cased, and the postcode is used verbatim — so the {@code householdId} is fully
 * deterministic: owners with the same last name and postcode compute the same value
 * independently, with no explicit linking.
 *
 * <p>The {@code householdId} is used both when {@link AssignHousehold} stamps it onto a
 * new owner and when the create-time duplicate check ({@link EnsureUniqueHousehold})
 * decides whether a new owner falls into an existing household, so the two cannot drift
 * apart. An owner with no postcode belongs to no household and gets a null id.
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
