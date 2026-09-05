package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}. Because it is derived purely from
 * the (last name, postcode) pair, any two owners sharing a last name (compared
 * case-insensitively with runs of whitespace collapsed to a single space) and postcode
 * receive the <em>same</em> identifier automatically — they are, by definition, the same
 * household — and no owner ever has to look up or backfill another. The value never changes
 * as more members join.
 *
 * <p>Runs after {@link BuildOwner} (so the Owner exists) and within the create transaction,
 * unconditionally: {@code sharesHousehold} no longer creates the link (the link is implicit
 * in the identifier); it only bypasses the later household-duplicate block (see
 * {@link RejectDuplicateIdentity}).
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String lastName = normalize(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode().trim();
        owner.setHouseholdId(householdId(lastName, postcode));
    }

    private static String householdId(String lastName, String postcode) {
        byte[] key = (lastName + "|" + postcode).getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        byte[] hash = digest.digest(key);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.substring(0, 12);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
