package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create request when the owner's city already contains {@link #CAPACITY} or
 * more owners. Runs before the owner is built, so the count reflects the owners that
 * already exist. A conflict is reported as a 409 by
 * {@link org.springframework.samples.petclinic.rest.escalation.CityAtCapacityExceptionHandler}.
 */
public class RequireCityCapacity {

    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
            .filter(existing -> city == null ? existing.getCity() == null : city.equals(existing.getCity()))
            .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, CAPACITY);
        }
    }
}
