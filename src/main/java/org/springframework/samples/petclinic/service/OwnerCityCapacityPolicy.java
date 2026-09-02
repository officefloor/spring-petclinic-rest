package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: an owner cannot be created in a city that already contains {@value #CITY_CAPACITY}
 * or more owners. Kept as a small, self-contained unit so the rule can be enforced from the create
 * flow without adding complexity to the controller or service.
 */
public final class OwnerCityCapacityPolicy {

    private static final int CITY_CAPACITY = 50;

    private OwnerCityCapacityPolicy() {
    }

    /**
     * Reject the given owner if its city already holds {@value #CITY_CAPACITY} or more owners.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     * @throws CityAtCapacityException if the owner's city is already at capacity
     */
    public static void rejectWhenCityAtCapacity(ClinicService clinicService, Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (city.equals(existing.getCity())) {
                count++;
            }
        }
        if (count >= CITY_CAPACITY) {
            throw new CityAtCapacityException();
        }
    }

    /** Thrown when an owner's city already contains {@value #CITY_CAPACITY} or more owners. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class CityAtCapacityException extends RuntimeException {
    }
}
