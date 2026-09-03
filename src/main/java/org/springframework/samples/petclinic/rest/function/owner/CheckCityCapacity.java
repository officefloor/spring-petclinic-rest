package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create when the owner's city already contains 50 or more owners, before
 * {@link SaveOwner} runs. Handled with 409 by {@code CityAtCapacityHandler}.
 */
public class CheckCityCapacity {

    static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(other -> !other.getId().equals(owner.getId()))
                .filter(other -> city.equals(normalize(other.getCity())))
                .count();
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(owner.getCity());
        }
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
