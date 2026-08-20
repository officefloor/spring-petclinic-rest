package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
 * {@code V2 + '|' + normalizedLastName + '|' + postcode}, the {@code V2} being the fixed identity
 * version tag so version-2 ids never reproduce a version-1 value. The household is therefore keyed on
 * (last name, postcode) alone, so any two owners with the same last name and postcode share the same
 * id automatically — nothing has to opt in and no existing owner is mutated. The {@code householdId}
 * is no longer part of {@link OwnerIdentityKey duplicate detection} (which is now the single identity
 * key); it still groups household members for the household-size count and membership rules. Runs on
 * the create pipeline before {@link SaveOwner}, so the stored id and the household-size count read the
 * same computed value.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String lastName = normalizeName(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        owner.setHouseholdId(deriveHouseholdId(lastName, postcode));
    }

    /**
     * The first 12 hex characters of SHA-256 over
     * {@code V2 + '|' + normalizedLastName + '|' + postcode}.
     */
    private static String deriveHouseholdId(String lastName, String postcode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    (Localities.IDENTITY_VERSION_TAG + "|" + lastName + "|" + postcode)
                            .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
