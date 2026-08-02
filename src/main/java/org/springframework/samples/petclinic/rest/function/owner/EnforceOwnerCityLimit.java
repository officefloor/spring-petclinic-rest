package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityLimitException;
import org.springframework.util.StringUtils;

/**
 * Rejects creating an owner once the owner's city already contains the maximum number
 * of owners - with a 400 via {@link OwnerCityLimitException}. The count is the number
 * of existing owners whose city matches the new owner's city, compared
 * case-insensitively (consistent with {@link NormalizeOwnerCity}). An owner with a
 * blank city is not subject to the cap.
 */
public class EnforceOwnerCityLimit {

    static final int MAX_OWNERS_PER_CITY = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws OwnerCityLimitException {
        String city = owner.getCity();
        if (!StringUtils.hasText(city)) {
            return;
        }
        long inCity = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existingCity -> existingCity != null && existingCity.equalsIgnoreCase(city))
                .count();
        if (inCity >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityLimitException(
                    "The maximum of " + MAX_OWNERS_PER_CITY + " owners for a single city has already been reached");
        }
    }
}
