package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for what a "household" is: two owners share one when their
 * last name and address match after normalization — trimmed, runs of whitespace
 * collapsed to a single space, and lower-cased, so the comparison ignores case and
 * incidental spacing. Shared by {@link EnsureUniqueHousehold} (which rejects an
 * unintended duplicate) and {@link AssignHousehold} (which joins an intended one), so
 * the two steps cannot drift apart: an owner admitted by one is matched by the other.
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

    /** Whether {@code existing} belongs to the household identified by the already
     *  normalized {@code lastName} and {@code address}. */
    static boolean matches(Owner existing, String normalizedLastName, String normalizedAddress) {
        return normalizedLastName.equals(normalize(existing.getLastName()))
                && normalizedAddress.equals(normalize(existing.getAddress()));
    }
}
