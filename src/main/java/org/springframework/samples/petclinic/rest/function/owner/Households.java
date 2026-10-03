package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for what a "household" is: two owners share one when their
 * last name and address match after normalization. The last name is trimmed, has runs
 * of whitespace collapsed to a single space and is lower-cased; the address uses the
 * canonical {@link OwnerAddresses} normalization (the same form that is stored and
 * returned), so the comparison always runs on the normalized address. Shared by
 * {@link EnsureUniqueHousehold} (which rejects an unintended duplicate) and
 * {@link AssignHousehold} (which joins an intended one), so the two steps cannot drift
 * apart: an owner admitted by one is matched by the other.
 */
final class Households {

    private Households() {
    }

    /** Lower-cases and collapses runs of whitespace to a single space so the comparison
     *  ignores case and incidental spacing differences. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** The canonical normalized address (see {@link OwnerAddresses}), used for every
     *  address comparison so it matches the stored and returned form. Never null. */
    static String normalizeAddress(String value) {
        String normalized = OwnerAddresses.normalize(value);
        return normalized == null ? "" : normalized;
    }

    /** Whether {@code existing} belongs to the household identified by the already
     *  normalized {@code lastName} and {@code address}. */
    static boolean matches(Owner existing, String normalizedLastName, String normalizedAddress) {
        return normalizedLastName.equals(normalize(existing.getLastName()))
                && normalizedAddress.equals(normalizeAddress(existing.getAddress()));
    }
}
