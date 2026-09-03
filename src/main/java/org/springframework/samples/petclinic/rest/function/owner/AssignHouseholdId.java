package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a deterministic {@code householdId} derived from the owner's normalized
 * lastName and postcode, so every owner sharing a lastName and postcode resolves to the
 * same identifier automatically. Runs before the household and identity checks.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        String postcode = owner.getPostcode();
        String key = IdentityRegion.code(owner) + "|" + CheckUniqueHousehold.normalize(owner.getLastName())
                + "|" + (postcode == null ? "" : postcode);
        owner.setHouseholdId(hash(key));
    }

    /** First 6 bytes (12 hex chars) of SHA-256({@code key}) as lower-case hex — stable across requests. */
    static String hash(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
