package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects owners that share a household, i.e. carry the same last name and the
 * same address compared case-insensitively with collapsed whitespace.
 */
final class Households {

    private Households() {
    }

    /** Trim, collapse internal whitespace and lower-case a value for comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** True when an existing owner has the candidate's last name and address. */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String lastName = normalize(candidate.getLastName());
        String address = normalize(candidate.getAddress());
        return existing.stream().anyMatch(o ->
            normalize(o.getLastName()).equals(lastName) && normalize(o.getAddress()).equals(address));
    }
}
