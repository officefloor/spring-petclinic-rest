package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose city already contains the maximum number of
 * owners, responding 409. Cities fill to a fixed capacity of {@value #CAPACITY}: once a
 * city holds that many owners no further owner may be created in it. Runs after
 * {@link ValidateOwnerFields} (which has already ensured the city is present) and before
 * {@link BuildOwner}, so the owner being created is not yet counted.
 */
public class EnsureCityCapacity {

    /** Maximum number of owners permitted per city. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = request.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, CAPACITY);
        }
    }
}
