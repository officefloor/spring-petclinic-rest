package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns every owner a deterministic {@code householdId}: the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}. The identifier is a pure function of the
 * household's last name and postcode, so two owners with the same last name at the same postcode
 * share it automatically — no back-fill and no dependence on {@code sharesHousehold} or the order
 * owners were created.
 *
 * <p>The shared householdId is what every household rule keys off: it forms part of each owner's
 * {@link OwnerIdentityKey identityKey}, it is the key the household-duplicate check
 * ({@link CheckHouseholdUnique}) matches on, and it is the key the household-size input to the
 * membership level ({@link AssignHouseholdMemberCount}) groups by. {@code sharesHousehold} no longer
 * creates the link; it only bypasses the duplicate block for a declared household member.
 */
public class AssignHouseholdId {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(deriveHouseholdId(normalize(owner.getLastName()), postcode(owner)));
    }

    /**
     * The first 12 hex characters of {@code SHA-256(normalizedLastName + '|' + postcode)} — a stable
     * identifier shared by every owner with the same last name and postcode.
     */
    private static String deriveHouseholdId(String lastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + postcode).getBytes(StandardCharsets.UTF_8));
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

    /** The postcode as supplied, trimmed; an absent postcode contributes the empty string. */
    private static String postcode(Owner owner) {
        String postcode = owner.getPostcode();
        return postcode == null ? "" : postcode.trim();
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
