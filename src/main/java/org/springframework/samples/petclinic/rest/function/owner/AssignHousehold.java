package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns every owner a deterministic {@code householdId}: the first 12 hex characters of SHA-256
 * over {@code normalizedLastName + '|' + postcode}. Owners with the same last name and postcode are
 * therefore the same household and share this id automatically, with no opt-in or back-fill. Runs
 * before the household duplicate block so that block can compare on the computed id.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + orEmpty(owner.getPostcode()) + "|V2";
        owner.setHouseholdId(hash12(key));
    }

    /** First 12 lower-case hex characters of SHA-256 over the UTF-8 bytes of {@code key}. */
    private static String hash12(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
