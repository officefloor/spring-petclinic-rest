package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner a deterministic {@code householdId} derived purely from its
 * {@code lastName} and {@code postcode}: the first twelve UPPER-case hex characters of
 * SHA-256 over {@code regionCodeV2 + '|' + normalizedLastName + '|' + postcode}, where the
 * version-2 region code is itself a pure function of the postcode. Because the id is a pure
 * function of those two fields, every owner with the same (normalized) last name and
 * postcode resolves to the same {@code householdId} automatically — they are, by
 * definition, the same household — regardless of registration order and independent of
 * the {@code sharesHousehold} create flag.
 *
 * <p>The flag no longer creates the household link; it only lets a second member of an
 * already-existing household bypass the duplicate block (see
 * {@link RejectDuplicateOwnerIdentity}). Everything that keys off the household —
 * duplicate detection and the household-size snapshot feeding the membership level —
 * reads this computed value.
 *
 * <p>No-ops (leaving {@code householdId} null, so the owner is a household of one) when
 * the owner has no last name or no postcode: without a postcode there is nothing to key
 * a household on.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        String lastName = normalize(owner.getLastName());
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return; // no household without both a last name and a postcode
        }
        owner.setHouseholdId(deriveHouseholdId(lastName, postcode));
    }

    /**
     * First twelve UPPER-case hex characters of SHA-256 over
     * {@code regionCodeV2 + '|' + lastName + '|' + postcode}. The version-2 region code (a
     * pure function of the postcode) rederives the householdId for version 2 while keeping
     * owners with the same last name and postcode on the same value.
     */
    private static String deriveHouseholdId(String lastName, String postcode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = PostcodeRegions.regionCodeV2(postcode) + "|" + lastName + "|" + postcode;
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Last-name key: lower-cased, trimmed, with internal whitespace runs collapsed to one space. */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        return collapsed.isEmpty() ? null : collapsed.toLowerCase(Locale.ROOT);
    }
}
