package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the deterministic version-2 {@code householdId}: the first 12 hex characters of SHA-256
 * over {@code normalizedLastName + '|' + postcode + '|' + "V2"}, where the last name is normalized
 * the same way {@link RequireUniqueOwnerHousehold} compares it (case-insensitive, whitespace
 * collapsed). The fixed {@code "V2"} tag is mixed in so no id equals its version-1 form, while
 * owners sharing a lastName and postcode still share the same value.
 *
 * <p>Because the value is derived purely from the household identity (lastName, postcode), every
 * owner is assigned one and owners with the same lastName and postcode share it automatically,
 * regardless of creation order and independent of whether the request opted into
 * {@code sharesHousehold}. No back-fill of existing owners is needed — they already carry the
 * same computed value.
 *
 * <p>Runs after {@link BuildOwner} (so the owner carries its lastName and postcode) and before
 * {@link RequireUniqueOwnerHousehold duplicate detection}, {@link AssignHouseholdSize the
 * household-size count} and {@link RequireUniqueOwnerIdentity the identity key}, all of which key
 * off this computed identifier.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(deriveHouseholdId(owner.getLastName(), owner.getPostcode()));
    }

    /** First 12 lower-case hex chars of SHA-256(normalizedLastName + '|' + postcode + '|' + "V2"). */
    static String deriveHouseholdId(String lastName, String postcode) {
        String key = RequireUniqueOwnerHousehold.normalize(lastName) + "|"
                + (postcode == null ? "" : postcode.trim()) + "|" + OwnerIdentityVersion.TAG;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
