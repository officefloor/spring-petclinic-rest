package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityOwnerCapException;

/**
 * Rejects creating an owner once {@link #CITY_CAP} owners already live in the new owner's city.
 * Runs after {@link NormalizeCity} (so the comparison uses the canonical city spelling) and before
 * {@link SaveOwner} so an over-cap owner never reaches the data store; the thrown escalation maps to
 * 400 Bad Request. The comparison ignores letter case.
 */
public class CheckCityOwnerCap {

    private static final int CITY_CAP = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws CityOwnerCapException {
        String city = owner.getCity();
        if (city == null) {
            return; // no city to enforce the cap against
        }
        long sameCity = ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        if (sameCity >= CITY_CAP) {
            throw new CityOwnerCapException(
                    "The maximum of " + CITY_CAP + " owners for " + city
                            + " has already been reached");
        }
    }
}
