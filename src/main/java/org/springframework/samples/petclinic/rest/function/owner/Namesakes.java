package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Namesake matching: an owner is a namesake of another when they share the same first and
 * last name, compared case-insensitively (and ignoring surrounding/internal whitespace).
 * Used to count how many existing owners already carry a new owner's name.
 */
final class Namesakes {

    private Namesakes() {
    }

    /**
     * Whether {@code owner} shares the given first and last name, comparing both in their
     * canonical (trimmed, whitespace-collapsed, lower-cased) forms.
     */
    static boolean matches(Owner owner, String firstName, String lastName) {
        return normalize(firstName).equals(normalize(owner.getFirstName()))
                && normalize(lastName).equals(normalize(owner.getLastName()));
    }

    private static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
