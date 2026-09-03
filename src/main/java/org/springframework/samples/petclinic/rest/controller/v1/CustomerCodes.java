package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's customerCode in the form {@code <CITY3>-<LAST3>-<NNNN>}: the
 * upper-cased first three letters of the city, the first three of the lastName, and
 * a per-city 4-digit sequence (one more than the owners already in that city).
 */
final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The {@code <CITY3>-<LAST3>-<NNNN>} code for {@code owner} given the {@code existing} owners. */
    static String build(Owner owner, Collection<Owner> existing) {
        long inCity = existing.stream()
            .filter(other -> owner.getCity().equalsIgnoreCase(other.getCity()))
            .count();
        return prefix(owner.getCity()) + "-" + prefix(owner.getLastName())
            + String.format("-%04d", inCity + 1);
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
