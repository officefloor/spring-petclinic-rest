package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the response with {@code capacityWarning} when the owner's city already holds
 * 40-49 owners (approaching the hard limit of 50 enforced by {@link CheckCityCapacity}),
 * before {@link SaveOwner} runs so the count excludes this owner.
 */
public class FlagCapacityWarning {

    static final int WARN_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = CheckCityCapacity.normalize(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(other -> !other.getId().equals(owner.getId()))
                .filter(other -> city.equals(CheckCityCapacity.normalize(other.getCity())))
                .count();
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CheckCityCapacity.CAPACITY);
    }
}
