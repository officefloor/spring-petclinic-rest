package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256
 * over {@code normalizedLastName + '|' + postcode}. Because the identifier is a pure function of the
 * last name and postcode, every owner with the same last name and postcode derives the same value and
 * so belongs to the same household automatically — there is no linking step and no back-fill.
 *
 * <p>The last name is normalized (trimmed, internal whitespace collapsed, lower-cased) before hashing,
 * so trivially different spellings map to one household. The identifier keys off the postcode alone,
 * not the free-text address, so a household needs a postcode: when the request has no postcode the
 * owner has no household and {@code householdId} is left null.
 *
 * <p>Note the change from earlier behaviour: {@code sharesHousehold} no longer creates the household
 * link (the link is now automatic); it only clears the soft-duplicate flag in
 * {@link AssignPossibleDuplicate}.
 *
 * <p>Runs after {@link BuildOwner} (so the entity, its last name and postcode exist) and before
 * {@link SaveOwner}, mutating the not-yet-persisted owner in place.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode -> no household
        }
        String lastName = normalize(owner.getLastName());
        owner.setHouseholdId(stableId(lastName, postcode.trim()));
    }

    /** Trim, collapse internal whitespace to a single space and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** First 12 hex characters of SHA-256 over the normalized last name and the postcode. */
    private static String stableId(String lastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 12);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
