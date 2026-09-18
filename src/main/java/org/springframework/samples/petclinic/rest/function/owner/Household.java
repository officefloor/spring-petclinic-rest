package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;

/**
 * Household identity shared by the create-owner steps. A household is the set of owners with the same
 * last name (compared case-insensitively with collapsed whitespace) and the same normalized address
 * (see {@link AddressNormalizer}). Used by {@link AssignHousehold} to give the members that opt into
 * sharing a single shared {@code householdId}, which then forms part of each owner's identity key.
 */
final class Household {

    private Household() {
    }

    /** Case-insensitive form with leading/trailing and repeated inner whitespace collapsed to one space. */
    static String canonical(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Whether {@code owner} belongs to the household identified by the given last name and address. */
    static boolean matches(Owner owner, String lastName, String address) {
        return canonical(owner.getLastName()).equals(canonical(lastName))
                && AddressNormalizer.normalize(owner.getAddress()).equals(AddressNormalizer.normalize(address));
    }

    /** A stable identifier for the household at the given last name and address, of the form {@code H-<hex>}. */
    static String idFor(String lastName, String address) {
        String hex = Sha256.hex(canonical(lastName) + '|' + AddressNormalizer.normalize(address));
        return "H-" + hex.substring(0, 12);
    }
}
