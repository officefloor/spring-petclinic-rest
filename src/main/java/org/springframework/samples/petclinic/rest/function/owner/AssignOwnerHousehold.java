package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 (upper-case) hex characters of
 * the SHA-256 of {@code normalizedLastName + '|' + postcode}. The id is derived purely from the
 * (lastName, postcode) pair, so every owner with the same lastName and postcode receives the same
 * value automatically, regardless of creation order and without any repository lookup or opt-in.
 *
 * <p>Because the household is now keyed on (lastName, postcode), owners sharing that pair are the
 * same household. This computed id is the single source of truth the household consumers key off:
 * {@link CheckOwnerHouseholdUnique} (the household-duplicate 409 block),
 * {@link DetectOwnerPossibleDuplicate} and {@link DeriveOwnerHouseholdSize}. The request's
 * {@code sharesHousehold} flag no longer creates the link — the link is implicit in the shared id —
 * it only bypasses the duplicate block (see {@link CheckOwnerHouseholdUnique}).
 *
 * <p>Runs after {@link BuildOwner} has created the entity and before {@link CheckOwnerHouseholdUnique}
 * (which sees the resolved {@code householdId}) and {@link SaveOwner} persists it.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(householdId(owner.getLastName(), owner.getPostcode()));
    }

    /**
     * The single source of truth for an owner's household identity: the first 12 upper-case hex
     * characters of the SHA-256 of {@code normalize(lastName) + '|' + postcode}. An absent postcode
     * contributes an empty segment. Two owners share a household exactly when this value is equal.
     */
    static String householdId(String lastName, String postcode) {
        String input = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString().toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
