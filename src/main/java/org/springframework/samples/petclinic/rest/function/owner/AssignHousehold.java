package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;

/**
 * Assigns the owner's {@code householdId} deterministically: the first 12 hexadecimal
 * characters of the SHA-256 digest over
 * {@code 'V2' + '|' + normalizedLastName + '|' + postcode}, mixing in the fixed version-2 tag
 * so the identifier is rederived and never repeats a version-1 value (see
 * {@link org.springframework.samples.petclinic.model.IdentityVersion}). Two
 * owners with the same last name and postcode therefore always derive the <em>same</em>
 * {@code householdId} — they are automatically the same household, with no opt-in and no
 * back-fill. Runs after {@link BuildOwner} (so the entity carries its stored last name and
 * postcode) and before {@link AssignHouseholdSize} (which counts the household).
 *
 * <p>Duplicate detection no longer keys on this value: it is now the single
 * {@code identityKey} (see {@link CheckOwnerIdentityUnique}). The {@code householdId}
 * remains a descriptive grouping used to size the household.
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        String key = IdentityVersion.TAG + "|" + normalize(owner.getLastName()) + "|"
                + nullToEmpty(owner.getPostcode());
        owner.setHouseholdId(hash12(key));
    }

    /** The first 12 lower-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash12(String value) {
        return Sha256.hex(value).substring(0, 12);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
