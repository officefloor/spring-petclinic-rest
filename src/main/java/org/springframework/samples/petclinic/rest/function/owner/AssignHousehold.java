package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
 * the fixed {@link IdentityVersion#TAG "V2"} version tag, the normalized lastName and the postcode,
 * each {@code '|'}-separated. Mixing the version tag in means no householdId produced under version 1
 * is ever produced again. The household is therefore
 * keyed on {@code (lastName, postcode)} alone — owners with the same lastName (compared
 * case-insensitively with collapsed whitespace) and the same postcode receive the same id
 * automatically, regardless of creation order and without inspecting or updating any other owner.
 *
 * <p>The {@code sharesHousehold} request flag no longer creates the link — the id is always computed
 * here — it only bypasses the household duplicate block in {@link CheckOwnerIdentityUnique}.
 * Everything that keys off the household ({@link CheckOwnerIdentityUnique} duplicate detection and
 * the {@link AssignHouseholdSize} household size) reads this computed id.
 *
 * <p>Runs after {@link BuildOwner} (the entity exists) and before those steps.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String lastName = normalize(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        owner.setHouseholdId(deriveHouseholdId(lastName, postcode));
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** The first 12 hex characters of SHA-256({@code "V2"} {@code '|'} normalizedLastName {@code '|'}
     *  postcode), so it is identical for every owner sharing that lastName and postcode yet distinct
     *  from any version-1 value. */
    private static String deriveHouseholdId(String normalizedLastName, String postcode) {
        try {
            String input = IdentityVersion.TAG + "|" + normalizedLastName + "|" + postcode;
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
