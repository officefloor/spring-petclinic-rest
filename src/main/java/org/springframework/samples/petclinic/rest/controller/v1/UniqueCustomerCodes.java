package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;

/**
 * De-duplicates a computed customerCode against existing owners. When the base code
 * collides, appends {@code '-<n>'} with the smallest {@code n >= 2} that is free.
 */
final class UniqueCustomerCodes {

    private UniqueCustomerCodes() {
    }

    /** The base code if free, otherwise {@code base + "-<n>"} for the smallest free n >= 2. */
    static String deduplicate(Collection<Owner> existing, String base) {
        Set<String> taken = existing.stream()
            .map(Owner::getCustomerCode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!taken.contains(base)) {
            return base;
        }
        int n = 2;
        while (taken.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }
}
