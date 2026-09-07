package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single home for how owners are grouped into a household and how a household's stable shared id is
 * derived. Parallels {@link OwnerIdentity} (the duplicate key) and {@link MemberId} (the
 * member id): the create pipeline's {@link AssignOwnerHousehold} goes through here, so the
 * grouping rule and the id derivation stay defined in one place.
 *
 * <p>The household is keyed on {@code (normalizedLastName, postcode)}: two owners belong to the
 * same household exactly when they share a normalized last name and postcode. The
 * {@code householdId} is derived DETERMINISTICALLY from those same two fields (see
 * {@link #id(Owner)}), so owners with the same last name and postcode compute the identical value
 * automatically — there is no longer a stored value to reuse.
 */
final class Household {

    private Household() {
    }

    /** Comparison key for a last name: leading/trailing and repeated whitespace collapsed, lower-cased. */
    static String lastNameKey(String lastName) {
        if (lastName == null) {
            return "";
        }
        return lastName.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** The postcode contribution to the household key: trimmed, or empty when absent. */
    private static String postcodeKey(String postcode) {
        return postcode == null ? "" : postcode.strip();
    }

    /**
     * The household's stable shared identifier for {@code owner}, derived deterministically as the
     * first 12 hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Owners
     * that share a normalized last name and postcode therefore compute the same value without any
     * coordination.
     */
    static String id(Owner owner) {
        return ShaHex.upperPrefix(lastNameKey(owner.getLastName()) + "|" + postcodeKey(owner.getPostcode()), 12);
    }
}
