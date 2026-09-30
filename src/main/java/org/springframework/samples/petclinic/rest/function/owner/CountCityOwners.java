package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Counts the owners already registered in the new owner's city, comparing case-insensitively,
 * and publishes the total so the per-city rules share a single accumulation. Runs before the
 * owner is saved, so the count excludes the owner being created; both {@link EnsureCityCapacity}
 * (the hard limit) and {@link AssignCapacityWarning} (the soft warning) read the published value
 * rather than re-scanning the table.
 */
public class CountCityOwners {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            @Qualified("cityOwnerCount") Out<Integer> cityOwnerCount) {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        cityOwnerCount.set(count);
    }
}
