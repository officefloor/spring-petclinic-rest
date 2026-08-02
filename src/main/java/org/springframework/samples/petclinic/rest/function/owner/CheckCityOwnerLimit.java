package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitException;

/**
 * Rejects creating an owner once their city already contains {@value #CITY_LIMIT}
 * owners — by throwing {@link CityOwnerLimitException} (400).
 *
 * <p>Cities are matched case-insensitively, so the cap is per-city regardless of how a
 * given owner spelt the city name.
 */
public class CheckCityOwnerLimit {

    static final int CITY_LIMIT = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityOwnerLimitException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long inCity = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCity() != null && existing.getCity().equalsIgnoreCase(city)) {
                inCity++;
            }
        }
        if (inCity >= CITY_LIMIT) {
            throw new CityOwnerLimitException(
                    "No more than " + CITY_LIMIT + " owners may be registered in a city");
        }
    }
}
