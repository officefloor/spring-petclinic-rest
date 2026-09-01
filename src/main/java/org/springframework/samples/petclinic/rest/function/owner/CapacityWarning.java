package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Response flag: true when this owner's city already holds 40-49 owners (approaching the
 * capacity limit of 50), otherwise false. Recomputed at response time from the same
 * city accumulation the per-city capacity rule counts (case-insensitive, whitespace
 * collapsed).
 */
final class CapacityWarning {

    private static final int WARN_FROM = 40;
    private static final int CAPACITY = 50;

    private CapacityWarning() {
    }

    static boolean isActive(Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        return count >= WARN_FROM && count < CAPACITY;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
