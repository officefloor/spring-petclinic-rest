package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the deterministic shared {@code householdId} for a create-owner request. A "household"
 * is now keyed purely on {@code (lastName, postcode)}: the id is the first 12 hex characters of
 * {@code SHA-256(normalizedLastName + '|' + postcode)}, so any two owners with the same last name
 * and postcode independently derive an identical value and are, by definition, the same household.
 * The last name is normalized case-insensitively with runs of whitespace collapsed to a single
 * space and leading/trailing whitespace trimmed; the postcode is used as supplied (trimmed).
 *
 * <p>The id is computed for <em>every</em> request and published as {@link HouseholdId} for
 * {@link AssignOwnerHousehold} to stamp onto the new owner (it is exposed on the response but no
 * longer keys duplicate detection — that is now the single {@link OwnerIdentityKey identity key}).
 * This step neither inspects existing owners nor stamps a link onto them.
 */
public class DetermineOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, Out<HouseholdId> householdId) {
        String lastName = canonical(request.getLastName());
        String postcode = request.getPostcode() == null ? "" : request.getPostcode().trim();
        householdId.set(new HouseholdId(deriveHouseholdId(lastName, postcode)));
    }

    /**
     * A stable household identifier: the first 12 hex characters of
     * {@code SHA-256('V2' + '|' + normalizedLastName + '|' + postcode)}, so every owner sharing the
     * same last name and postcode independently derives an identical (version-2) value.
     */
    static String deriveHouseholdId(String normalizedLastName, String postcode) {
        // Version 2: mix in the fixed 'V2' tag so every household id changes and no version-1
        // value is reproduced. The household is still keyed on (lastName, postcode).
        String seed = OwnerIdentityVersion.TAG + "|" + normalizedLastName + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
