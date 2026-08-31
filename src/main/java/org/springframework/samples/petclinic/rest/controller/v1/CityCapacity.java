package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the per-city capacity warning: a city is "approaching capacity" when it
 * already holds 40 to 49 owners, just short of the hard limit of 50.
 */
final class CityCapacity {

    private CityCapacity() {
    }

    /** True when {@code owner}'s city holds 40-49 owners (below the hard limit of 50). */
    static boolean approaching(Collection<Owner> owners, Owner owner) {
        int count = CityCodes.count(owners, owner);
        return count >= 40 && count < 50;
    }
}
