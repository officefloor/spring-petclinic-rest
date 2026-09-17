package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * The single definition of an owner "household": owners sharing the same last name and
 * postcode. The last name is compared case-insensitively with runs of whitespace collapsed
 * to a single space; the postcode is taken as stored. Provides the deterministic identifier
 * shared by the household.
 *
 * <p>The identifier is derived deterministically from the normalized last name and postcode
 * — the first 12 upper-case hex characters of SHA-256 over {@code normalizedLastName + '|' +
 * postcode} — so every owner with the same last name and postcode resolves to the same value
 * regardless of creation order, and no owner-to-owner linking is needed.
 *
 * @see AssignHousehold stamps each owner with its computed identifier.
 * @see EnsureUniqueIdentity blocks a second owner in the same household unless it opts in.
 */
final class Household {

    /** Length of the household identifier, in hex characters. */
    private static final int ID_LENGTH = 12;

    private Household() {
    }

    /** The stable identifier shared by every owner in the household keyed by {@code lastName}
     *  and {@code postcode}. */
    static String id(String lastName, String postcode) {
        String key = normalizeName(lastName) + "|" + (postcode == null ? "" : postcode);
        return Sha256.hex(key, ID_LENGTH);
    }

    /** A last name trimmed and lower-cased, with internal whitespace runs collapsed to a single space. */
    private static String normalizeName(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
