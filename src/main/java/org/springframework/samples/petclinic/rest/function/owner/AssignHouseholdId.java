package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code householdId}: a stable identifier shared by all owners who live in the
 * same household, i.e. share a last name and address. It is the first 12 upper-cased hex characters
 * of the SHA-256 of {@code '<lastName>|<address>'}, with the last name normalized exactly as
 * {@link CheckHouseholdUnique} normalizes it (lower-cased, runs of whitespace collapsed, trimmed) and
 * the address normalized to its canonical form (see {@link AddressNormalizer}).
 *
 * <p>Because the id derives only from that normalized pair, two owners who intentionally share a
 * household (accepted via {@code sharesHousehold}) deterministically receive the same value without
 * any lookup. Runs after {@link BuildOwner} has mapped the request onto the entity and before
 * {@link SaveOwner}, so the assigned id is persisted and returned.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + AddressNormalizer.normalize(owner.getAddress());
        owner.setHouseholdId(shaHex(key, 12));
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimmed. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** First {@code n} upper-case hex characters of SHA-256 of {@code value}. */
    private static String shaHex(String value, int n) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, n);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
