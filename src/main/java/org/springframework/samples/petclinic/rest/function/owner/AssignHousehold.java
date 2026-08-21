package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's stable, shared {@code householdId}, derived deterministically from the
 * household's identity — its {@code lastName} (compared case-insensitively with collapsed
 * whitespace) and {@code postcode}. The value is the first 12 hex characters of
 * SHA-256 over {@code normalizedLastName + '|' + postcode}, so every owner with the same
 * lastName and postcode computes the same identifier automatically — no lookup or backfill of
 * other owners is needed.
 *
 * <p>The identifier is computed for every create with a postcode, independent of
 * {@code sharesHousehold}: sharing a household is now an intrinsic consequence of the
 * lastName/postcode pair, and {@code sharesHousehold} only governs whether a would-be household
 * duplicate is blocked (see {@link EnsureUniqueIdentity}). An owner without a postcode has no
 * household, so its {@code householdId} is left null.
 *
 * <p>Runs after {@code build}, so the new Owner entity exists but is not yet saved.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no household without a postcode
        }
        String lastName = normalize(owner.getLastName());
        owner.setHouseholdId(householdId(lastName, postcode));
    }

    /** Stable 12-hex-char identifier for a household: the first 12 hex characters of SHA-256 over
     *  the normalized lastName and postcode, so every member of the same household computes the
     *  same value. */
    private static String householdId(String lastName, String postcode) {
        return sha256hex(lastName + "|" + postcode).substring(0, 12);
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
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Case-insensitive with collapsed whitespace: trim, fold internal whitespace runs to a
     *  single space, and lower-case. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
