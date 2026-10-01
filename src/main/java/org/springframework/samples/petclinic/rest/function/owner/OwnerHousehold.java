package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for the owner-household identity rule. Two owners count as the same
 * household when their last names match (compared case-insensitively, whitespace collapsed) and
 * their addresses match in their normalized form (see {@link OwnerAddress#normalize}: whitespace
 * collapsed, upper-cased, common abbreviations expanded). This class owns both the comparison
 * ({@link #sameHousehold}, built on {@link #normalize}) and the derivation of the stable shared
 * {@link #householdId} that identifies a household.
 *
 * <p>Used by {@link AssignOwnerHousehold} to find the household's existing members and to compute
 * the id they share, and by {@link OwnerIdentity} to derive the household segment of an owner's
 * identity key — so the definition of "same household" lives here and nowhere else.
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
     * address — the one place that defines what "same household" means. Last names are reduced
     * through {@link #normalize} and addresses through {@link OwnerAddress#normalize} before
     * comparison, so differently cased, spaced or abbreviated values that mean the same thing still
     * match.
     */
    static boolean sameHousehold(Owner existing, String lastName, String address) {
        return normalize(existing.getLastName()).equals(normalize(lastName))
                && OwnerAddress.normalize(existing.getAddress())
                        .equals(OwnerAddress.normalize(address));
    }

    /**
     * Derives the stable household identifier shared by every owner in the same household. Two
     * owners whose normalized last name and address match produce the same value, so the id can be
     * computed independently for each joining owner without coordinating through the database. The
     * value is {@code "HH-"} followed by the first 12 upper-case hex characters of the SHA-256 of
     * the normalized {@code "<lastName>|<address>"}.
     */
    static String householdId(String lastName, String address) {
        String key = normalize(lastName) + "|" + OwnerAddress.normalize(address);
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
