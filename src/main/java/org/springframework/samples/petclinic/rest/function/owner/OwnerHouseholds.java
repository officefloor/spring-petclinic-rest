package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;

/**
 * The single definition of what a "household" is: owners sharing the same last name and
 * postcode, compared case-insensitively with runs of whitespace collapsed to a single
 * space. The {@code householdId} is derived deterministically from those two fields, so
 * every owner with the same last name and postcode computes the same id automatically,
 * regardless of creation order.
 *
 * <p>Shared by {@link AssignOwnerHousehold} (which stamps the id),
 * {@link AssignOwnerHouseholdSize} (which counts members) and
 * {@link AssignOwnerMembershipLevelCap} (which caps a new member's level), so all decide
 * membership identically.
 */
final class OwnerHouseholds {

    private static final int ID_HEX_LENGTH = 12;

    private OwnerHouseholds() {
    }

    /**
     * A stable identifier for the household of an owner with this {@code lastName} and
     * {@code postcode}: the first {@value #ID_HEX_LENGTH} hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Every member computes the same value, so
     * it is deterministic and independent of creation order.
     */
    static String householdId(String lastName, String postcode) {
        String key = canonical(lastName) + "|" + trimmed(postcode);
        return Sha256.upperHex(key, ID_HEX_LENGTH);
    }

    /**
     * The existing owners that belong to the household identified by {@code lastName} and
     * {@code postcode} — those whose own last name and postcode hash to the same
     * {@link #householdId}.
     */
    static List<Owner> members(Iterable<Owner> owners, String lastName, String postcode) {
        String householdId = householdId(lastName, postcode);
        List<Owner> members = new ArrayList<>();
        for (Owner existing : owners) {
            if (householdId.equals(householdId(existing.getLastName(), existing.getPostcode()))) {
                members.add(existing);
            }
        }
        return members;
    }

    /** Trim, collapse internal whitespace to a single space, and lower-case for comparison. */
    private static String canonical(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** The trimmed value, or the empty string when absent. */
    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}
