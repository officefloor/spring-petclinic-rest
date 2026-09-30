package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of household identity, used by {@link AssignOwnerHousehold} (which
 * stamps the householdId) and by the household-keyed rules (duplicate detection, household
 * size), so the rule lives in one place.
 *
 * <p>A household is keyed on {@code (lastName, postcode)}: two owners are in the same
 * household when their normalized lastName and postcode match. The householdId is therefore
 * <em>deterministic</em> — the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode} — so owners with the same lastName and
 * postcode share it automatically, with no lookup. lastName is normalized case-insensitively
 * with collapsed whitespace; the postcode is used as given (empty when absent).
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * The deterministic householdId for the given lastName and postcode: the first 12 hex
     * characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Owners with the
     * same lastName and postcode therefore compute the same householdId automatically.
     */
    static String householdId(String lastName, String postcode) {
        String input = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256Hex(input).substring(0, 12);
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

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-case and collapse whitespace runs to a single space for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
