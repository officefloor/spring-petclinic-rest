package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Business rule: flag a city that is approaching the {@value #CITY_CAPACITY}-owner hard limit for the
 * response once it already holds between {@value #WARN_FROM} and {@value #CITY_CAPACITY}-1 owners.
 * Mirrors the count used by {@link OwnerCityCapacityPolicy} (which hard-rejects at
 * {@value #CITY_CAPACITY}). Kept as a small, self-contained unit so the flag can be derived during
 * owner-to-DTO mapping without adding complexity to the mapper, controller or service.
 */
@Component
public class OwnerCapacityWarningPolicy {

    private static final int WARN_FROM = 40;

    private static final int CITY_CAPACITY = 50;

    private static ClinicService clinicService;

    OwnerCapacityWarningPolicy(ClinicService clinicService) {
        OwnerCapacityWarningPolicy.clinicService = clinicService;
    }

    /**
     * @param owner the owner whose city to total
     * @return true when the owner's city already holds {@value #WARN_FROM}..{@value #CITY_CAPACITY}-1 owners
     */
    public static boolean capacityWarning(Owner owner) {
        String city = owner.getCity();
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (city != null && city.equals(existing.getCity())) {
                count++;
            }
        }
        return count >= WARN_FROM && count < CITY_CAPACITY;
    }
}
