package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs on {@code POST /api/owners} before {@link BuildOwner} (so no owner is created on conflict):
 * rejects the request with 409 via {@link CityAtCapacityException} when the request's city already
 * contains {@link #MAX_OWNERS_PER_CITY} or more owners, compared case-insensitively with collapsed
 * whitespace.
 */
public class CheckOwnerCityCapacity {

    /** Maximum owners allowed per city; the next create in a full city is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity())) && ++count >= MAX_OWNERS_PER_CITY) {
                throw new CityAtCapacityException(request.getCity());
            }
        }
    }

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
