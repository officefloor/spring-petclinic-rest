package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that assigns the owner's deterministic {@code householdId}.
 *
 * <p>The identifier is the first 12 upper-case hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode} (last name trimmed, internal whitespace collapsed and
 * lower-cased). Because it is derived purely from the household key, every owner sharing the same
 * last name and postcode receives the SAME value automatically, regardless of creation order and
 * without scanning or back-filling other owners. An owner with no postcode has no household, so its
 * {@code householdId} is {@code null}.
 *
 * <p>Runs after {@link BuildOwner}, so it works on the built entity, and before
 * {@link EnsureUniqueIdentity}, so duplicate detection and {@link AssignHouseholdSize} see the
 * finalized value. Note {@code sharesHousehold} plays no part here: it only bypasses the duplicate
 * block downstream; the household link itself is always this computed value.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(householdId(owner.getLastName(), owner.getPostcode()));
    }

    /**
     * The deterministic householdId for a {@code (lastName, postcode)} pair, or {@code null} when the
     * postcode is absent (an owner with no postcode is not part of a household).
     */
    static String householdId(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String key = normalize(lastName) + "|" + postcode.trim();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
