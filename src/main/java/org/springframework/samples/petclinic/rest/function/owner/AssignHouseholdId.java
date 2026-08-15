package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that assigns a stable shared {@code householdId} when the
 * request sets {@code sharesHousehold=true}. The id is derived deterministically from the
 * normalized lastName and address — the same normalization {@link RequireUniqueHousehold} uses
 * to detect a shared household — so every owner joining the same household is given the identical
 * identifier. Owners that do not share a household are left with no householdId.
 */
public class AssignHouseholdId {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        owner.setHouseholdId(householdId(request.getLastName(), request.getAddress()));
    }

    /** Stable identifier for the household sharing {@code lastName} at {@code address}. */
    static String householdId(String lastName, String address) {
        String key = normalize(lastName) + "|" + normalize(address);
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
