package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * City matching: two owners are in the same city when their city names are equal, compared
 * case-insensitively (and ignoring surrounding/internal whitespace). Used to count how many
 * existing owners already live in a new owner's city.
 */
final class Cities {

    private Cities() {
    }

    /**
     * Whether {@code owner} lives in the given city, comparing both in their canonical
     * (trimmed, whitespace-collapsed, lower-cased) forms.
     */
    static boolean matches(Owner owner, String city) {
        return normalize(city).equals(normalize(owner.getCity()));
    }

    private static String normalize(String city) {
        if (city == null) {
            return "";
        }
        return city.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
