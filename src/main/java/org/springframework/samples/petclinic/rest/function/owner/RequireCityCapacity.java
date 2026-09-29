package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request with a 409 (via {@code CityAtCapacityExceptionHandler}) when the
 * owner's city already contains {@code CAPACITY} (50) or more owners, compared case-insensitively.
 * The new owner is not persisted yet, so every match found is an existing owner.
 */
public class RequireCityCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long count = ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city);
        }
    }
}
