package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of what makes owners share a household: the same last name (compared
 * case-insensitively after collapsing runs of whitespace) and the same address in its normalized
 * form (see {@link AddressNormalizer}). Shared by {@link RequireUniqueHousehold} (which rejects an
 * un-flagged duplicate) and {@link AssignHousehold} (which links a {@code sharesHousehold} owner to
 * the existing members), so both apply the same rule and cannot drift apart.
 *
 * <p>Also owns the deterministic {@code householdId} derived from that key: joining the same household
 * always yields the same value and it never changes over time.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class HouseholdKey {

    private final String lastName;

    private final String address;

    private HouseholdKey(String lastName, String address) {
        this.lastName = lastName;
        this.address = address;
    }

    /** The household key for the given last name and address. */
    static HouseholdKey of(String lastName, String address) {
        return new HouseholdKey(normalize(lastName), AddressNormalizer.normalize(address));
    }

    /** True when the existing owner belongs to this household. */
    boolean matches(Owner owner) {
        return this.lastName.equals(normalize(owner.getLastName()))
                && this.address.equals(AddressNormalizer.normalize(owner.getAddress()));
    }

    /** A stable, shared identifier derived from the normalized last name and address. */
    String householdId() {
        String key = this.lastName + "\u0000" + this.address;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** Lower-cases and collapses runs of whitespace to a single space for case- and
     *  whitespace-insensitive comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
