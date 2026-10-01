package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for the owner-household identity rule. Two owners count as the same
 * household when their last names match and their addresses match, each compared case-insensitively
 * with surrounding and repeated whitespace collapsed. This class owns both the comparison
 * ({@link #sameHousehold}, built on {@link #normalize}) and the derivation of the stable shared
 * {@link #householdId} that identifies a household.
 *
 * <p>Used by {@link CheckOwnerHouseholdUnique} to reject a non-opted-in duplicate, and by
 * {@link AssignOwnerHousehold} to find the household's existing members and to compute the id they
 * share — so the definition of "same household" lives here and nowhere else.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * Reduces a value to its comparison form: trimmed, inner runs of whitespace collapsed to a
     * single space, and lower-cased. A {@code null} value yields an empty string.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Tells whether an existing owner belongs to the household identified by the given last name and
     * address — the one place that defines what "same household" means. Both sides are reduced
     * through {@link #normalize} before comparison, so differently cased or spaced values that mean
     * the same thing still match.
     */
    static boolean sameHousehold(Owner existing, String lastName, String address) {
        return normalize(existing.getLastName()).equals(normalize(lastName))
                && normalize(existing.getAddress()).equals(normalize(address));
    }

    /**
     * Derives the stable household identifier shared by every owner in the same household. Two
     * owners whose normalized last name and address match produce the same value, so the id can be
     * computed independently for each joining owner without coordinating through the database. The
     * value is {@code "HH-"} followed by the first 12 upper-case hex characters of the SHA-256 of
     * the normalized {@code "<lastName>|<address>"}.
     */
    static String householdId(String lastName, String address) {
        String key = normalize(lastName) + "|" + normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "HH-" + sb;
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
