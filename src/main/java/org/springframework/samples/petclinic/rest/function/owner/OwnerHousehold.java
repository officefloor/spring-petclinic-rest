package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Shared household rules for the create-owner pipeline. The household identifier is now
 * <em>deterministic</em>: it is derived from the owner's last name and postcode alone, so any two
 * owners with the same (normalized) last name and the same postcode resolve to the same id without
 * coordination. Concretely the id is the first 12 hex characters of {@code SHA-256} over
 * {@code normalizedLastName + '|' + postcode}. Because the id is a pure function of those two
 * fields, sharing a household is no longer something a request opts into to create a link - it
 * happens automatically. Duplicate detection is now the single {@code identityKey}, so there is no
 * separate household-duplicate block; the {@code sharesHousehold} flag now only suppresses the
 * soft-match flag (see {@link AssignPossibleDuplicate}) for a declared household member.
 */
final class OwnerHousehold {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private OwnerHousehold() {
    }

    /** Canonical form of a last name for household keying: trimmed, whitespace-collapsed, lower-cased. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return WHITESPACE.matcher(value.trim()).replaceAll(" ").toLowerCase();
    }

    /**
     * Deterministic identifier for the household of the given last name and postcode: the first 12
     * hex characters of {@code SHA-256(normalizedLastName + '|' + postcode)}. A null postcode
     * contributes an empty segment, so owners with the same last name and no postcode still share.
     */
    static String id(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256hex(key).substring(0, 12);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
