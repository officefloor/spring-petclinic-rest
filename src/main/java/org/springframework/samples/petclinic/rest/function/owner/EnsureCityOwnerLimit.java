package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitExceededException;

/**
 * Rejects creation of an owner once the new owner's city already contains the maximum
 * number of owners. At most {@link #CITY_LIMIT} owners may share the same city; the
 * creation that would push the count past that limit is rejected.
 */
public class EnsureCityOwnerLimit {

    /** Maximum owners that may share the same city. */
    static final int CITY_LIMIT = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityOwnerLimitExceededException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long alreadyInCity = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            String existingCity = existing.getCity();
            if (existingCity != null && existingCity.equalsIgnoreCase(city)) {
                alreadyInCity++;
            }
        }
        if (alreadyInCity >= CITY_LIMIT) {
            throw new CityOwnerLimitExceededException(
                    "The maximum of " + CITY_LIMIT + " owners for " + city
                            + " has already been reached");
        }
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && java.util.Objects.equals(a.getId(), b.getId());
    }
}
