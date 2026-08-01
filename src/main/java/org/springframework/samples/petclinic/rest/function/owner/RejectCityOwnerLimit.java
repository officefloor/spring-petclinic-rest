package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerLimitExceededException;

/**
 * Rejects creating an owner once the owner's city already contains the maximum number of owners,
 * responding 400 via {@link CityOwnerLimitExceededException}. Owners are grouped by city (matched
 * ignoring letter case); when {@value #MAX_OWNERS_PER_CITY} owners already share the new owner's
 * city, no further owner may be created there. Runs after {@code normalizeCity} (so the city has
 * its canonical spelling) and before {@code save}.
 */
public class RejectCityOwnerLimit {

    static final int MAX_OWNERS_PER_CITY = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityOwnerLimitExceededException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> existing.getCity() != null)
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (inCity >= MAX_OWNERS_PER_CITY) {
            throw new CityOwnerLimitExceededException(
                    "The maximum number of owners for this city has already been reached");
        }
    }
}
