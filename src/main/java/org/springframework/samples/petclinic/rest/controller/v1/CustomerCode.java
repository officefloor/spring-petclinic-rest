package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code customerCode} in the format {@code '<CITY3>-<LAST3>-<NNNN>'}:
 * the upper-cased first three letters of the city, the first three of the last name, and a
 * per-city 4-digit sequence (one more than the owners already registered in that city).
 */
final class CustomerCode {

    private CustomerCode() {
    }

    static String of(Owner owner, Collection<Owner> existing) {
        long inCity = existing.stream()
            .filter(o -> owner.getCity().equalsIgnoreCase(o.getCity()))
            .count();
        return String.format("%s-%s-%04d", head(owner.getCity()), head(owner.getLastName()), inCity + 1);
    }

    private static String head(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
