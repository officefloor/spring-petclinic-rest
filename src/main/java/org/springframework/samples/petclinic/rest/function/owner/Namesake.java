package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner "namesake": two owners are namesakes when they share
 * the same first name and last name, compared case-insensitively (leading and trailing
 * whitespace ignored). Provides the membership test used to count how many existing owners
 * a new owner shares a name with.
 *
 * @see CountNamesakes counts the matching owners at registration.
 */
final class Namesake {

    private Namesake() {
    }

    /** Whether {@code owner} is a namesake of the owner named {@code firstName} {@code lastName}. */
    static boolean matches(Owner owner, String firstName, String lastName) {
        return normalize(owner.getFirstName()).equals(normalize(firstName))
                && normalize(owner.getLastName()).equals(normalize(lastName));
    }

    /** A name trimmed and lower-cased, so comparison is case-insensitive. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
