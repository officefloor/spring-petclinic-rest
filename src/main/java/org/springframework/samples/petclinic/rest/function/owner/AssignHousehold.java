package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's <em>deterministic</em> {@code householdId}: the first 12 hex characters
 * of SHA-256 over {@code normalizedLastName + '|' + postcode}. Because the value is derived
 * purely from the last name and postcode, every owner sharing those two fields computes the
 * <em>same</em> identifier automatically — no cross-owner lookup and no back-fill are needed.
 *
 * <p>The {@code householdId} is no longer part of duplicate detection (that is now the single
 * {@code identityKey}, keyed on telephone, email and {@code soundex(lastName)}); it survives only
 * to size the household for the later membership rules (see {@link CountHouseholdMembers}).
 *
 * <p>Runs after {@link BuildOwner} (so the entity — hence its last name and postcode — exists)
 * and before {@link CheckOwnerIdentityUnique}, mutating the built {@link Owner} in place.
 */
public class AssignHousehold {

    public void service(@Val Owner built) {
        String lastName = normalize(built.getLastName());
        String postcode = built.getPostcode() == null ? "" : built.getPostcode();
        built.setHouseholdId(householdId(lastName, postcode));
    }

    /** A stable household identifier: first 12 hex chars of SHA-256 over the normalized last
     *  name and postcode, so all members of the same household derive the same value. */
    static String householdId(String normalizedLastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
