package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that assigns the deterministic {@code householdId}. The id is
 * derived from the normalized lastName and the postcode, so any two owners with the same lastName
 * and postcode are automatically given the identical identifier and are treated as one household.
 * It is one component of the consolidated {@link OwnerIdentityKey}. The id no longer depends on
 * {@code sharesHousehold} (which now only bypasses the household duplicate block). Owners with no
 * postcode have no household and are left with no householdId.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner) {
        owner.setHouseholdId(forRequest(request));
    }

    /**
     * The householdId a create request maps to: the deterministic id for its {@code lastName} and
     * {@code postcode}, or {@code null} when no postcode is supplied (no household).
     */
    static String forRequest(OwnerFieldsDto request) {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        return householdId(request.getLastName(), postcode);
    }

    /** Stable identifier for the household sharing {@code lastName} at {@code postcode}. */
    static String householdId(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return "HH-" + sha256Hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
