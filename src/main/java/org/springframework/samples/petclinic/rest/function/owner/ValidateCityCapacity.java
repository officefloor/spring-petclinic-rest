package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Runs after {@link ValidateUniqueIdentity} and before {@link BuildOwner}. A read-only
 * validation gate: rejects the create with a 409 when the owner's city already holds
 * {@link CityPopulation#CAPACITY} or more owners, so no city grows past capacity. Delegates the
 * count and the limit to the shared {@link CityPopulation} rule. Publishes nothing; it only
 * escalates on conflict.
 */
public class ValidateCityCapacity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        int count = CityPopulation.count(city, ownerRepository);
        if (count >= CityPopulation.CAPACITY) {
            throw new CityAtCapacityException(
                    "City " + city + " already contains " + CityPopulation.CAPACITY + " or more owners");
        }
    }
}
