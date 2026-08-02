package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitExceededException;

/**
 * Rejects creating an owner once {@value #CITY_LIMIT} owners already share the new
 * owner's city. Matching ignores letter case, so a city is capped regardless of
 * how it is spelt.
 */
public class EnsureCityOwnerLimit {

    static final int CITY_LIMIT = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityOwnerLimitExceededException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long existingInCity = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existingCity -> existingCity != null && existingCity.equalsIgnoreCase(city))
                .count();
        if (existingInCity >= CITY_LIMIT) {
            throw new CityOwnerLimitExceededException(
                    "City " + city + " has reached its limit of " + CITY_LIMIT + " owners");
        }
    }
}
