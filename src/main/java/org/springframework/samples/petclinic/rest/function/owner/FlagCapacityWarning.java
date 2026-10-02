package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Populates the response-only {@link Owner#isCapacityWarning() capacityWarning} flag: true when
 * the owner's city is {@link CityPopulation#approachingCapacity approaching} the hard capacity
 * limit (it already holds between 40 and 49 owners), otherwise false. Delegates the count and the
 * band to the shared {@link CityPopulation} rule, the same authority {@link ValidateCityCapacity}
 * rejects at. Mutates the loaded owner in place so the responder's mapper carries the flag onto
 * the DTO.
 */
public class FlagCapacityWarning {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = CityPopulation.count(owner.getCity(), ownerRepository);
        owner.setCapacityWarning(CityPopulation.approachingCapacity(count));
    }
}
