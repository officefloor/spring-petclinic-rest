package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner response should carry a capacity warning: true when the owner's city
 * already holds between {@value #WARN_FROM} and {@value #WARN_TO} owners inclusive, approaching the
 * hard limit of 50 enforced by {@link RejectCityAtCapacity}. Cities are compared case-insensitively
 * with collapsed whitespace, matching that rejection.
 */
public final class CityCapacity {

    private static final int WARN_FROM = 40;
    private static final int WARN_TO = 49;

    private CityCapacity() {
    }

    public static boolean approaching(Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count >= WARN_FROM && count <= WARN_TO;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
