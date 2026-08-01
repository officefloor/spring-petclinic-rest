package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityLimitException;

/**
 * Rejects creating an owner once {@value #CITY_LIMIT} owners already live in the owner's
 * city (matched case- and whitespace-insensitively), by throwing an
 * {@link OwnerCityLimitException}, handled as 400.
 */
public class RejectOwnerCityLimit {

    static final int CITY_LIMIT = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerCityLimitException {
        if (owner.getCity() == null) {
            return;
        }
        long cityCount = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (DuplicateMatching.matches(existing.getCity(), owner.getCity())) {
                cityCount++;
            }
        }
        if (cityCount >= CITY_LIMIT) {
            throw new OwnerCityLimitException(
                "The maximum number of owners for this city has already been reached");
        }
    }
}
