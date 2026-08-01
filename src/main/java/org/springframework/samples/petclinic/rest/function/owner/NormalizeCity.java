package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Normalises the new owner's city. If another owner already exists in the same city (matched
 * ignoring letter case), that owner's exact spelling of the city is reused so a city keeps one
 * canonical spelling. Otherwise the supplied city is title-cased. Runs after {@code build} and
 * before {@code assignCustomerCode} so the customer code is derived from the normalised city.
 */
public class NormalizeCity {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            String existingCity = existing.getCity();
            if (existingCity != null && existingCity.equalsIgnoreCase(city)) {
                owner.setCity(existingCity);
                return;
            }
        }
        owner.setCity(CityNormalizer.titleCase(city));
    }
}
