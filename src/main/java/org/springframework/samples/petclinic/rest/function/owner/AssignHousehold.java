package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of
 * SHA-256 over {@code normalizedLastName + '|' + postcode}. Because the value is derived
 * purely from the (last name, postcode) pair, every owner sharing that pair computes the
 * same identifier automatically — the household is keyed on the pair, not created by an
 * explicit opt-in.
 *
 * <p>The last name is normalized (compared case-insensitively with collapsed whitespace)
 * before hashing; the postcode is used verbatim, treated as empty when absent. So two
 * owners with the same last name and postcode always share a household, regardless of
 * creation order or the {@code sharesHousehold} flag (which now only bypasses the
 * household-duplicate block — see {@link EnsureUniqueHousehold}).
 *
 * <p>Runs after {@link BuildOwner} (which produces the {@link Owner}) and before
 * {@link EnsureUniqueHousehold}/{@link SaveOwner}; it mutates the built owner in place
 * (see {@code @Val} semantics). The computed id feeds duplicate detection (via the
 * {@code identityKey}) and the household-size count.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String lastName = normalize(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        owner.setHouseholdId(householdId(lastName, postcode));
    }

    /** Deterministic 12-char hex id over the normalized last name and postcode. */
    private static String householdId(String lastName, String postcode) {
        return sha256hex(lastName + "|" + postcode).substring(0, 12);
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase();
    }
}
