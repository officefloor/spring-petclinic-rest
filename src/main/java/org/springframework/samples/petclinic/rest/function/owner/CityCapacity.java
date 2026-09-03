package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * City occupancy relative to the hard capacity limit of {@value #CAPACITY} owners
 * (compared case-insensitively, matching {@link RequireCityCapacity}). A city is
 * {@link #approaching} capacity once it already holds between {@value #WARN_FROM} and
 * {@value #CAPACITY} minus one owners.
 */
public final class CityCapacity {

    private static final int CAPACITY = 50;

    private static final int WARN_FROM = 40;

    private CityCapacity() {
    }

    public static boolean approaching(String city, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
                .filter(owner -> city != null && city.equalsIgnoreCase(owner.getCity()))
                .count();
        return count >= WARN_FROM && count < CAPACITY;
    }
}
