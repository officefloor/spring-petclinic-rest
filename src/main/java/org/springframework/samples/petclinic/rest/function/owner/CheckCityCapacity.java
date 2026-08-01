package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityCapacityException;

/**
 * Rejects creating an owner once {@value #CITY_LIMIT} owners already live in the same
 * city (compared ignoring case and surrounding/repeated whitespace), by throwing
 * {@link CityCapacityException}, which is handled as a 400 Bad Request.
 */
public class CheckCityCapacity {

    static final int CITY_LIMIT = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityCapacityException {
        String key = DuplicateKey.normalize(owner.getCity());
        if (key == null) {
            return;
        }
        long inCity = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (key.equals(DuplicateKey.normalize(existing.getCity()))) {
                inCity++;
            }
        }
        if (inCity >= CITY_LIMIT) {
            throw new CityCapacityException(
                    "Cannot register more than " + CITY_LIMIT + " owners in a single city");
        }
    }
}
