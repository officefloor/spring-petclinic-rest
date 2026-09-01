package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects 409 when the built owner's city already contains 50 or more owners, compared
 * case-insensitively after trimming. The new owner is not yet saved, so a city holding
 * exactly 50 existing owners rejects the 51st.
 */
public class RejectOwnerCityAtCapacity {

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws CityAtCapacityException {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && city.equals(normalize(other.getCity())) && ++count >= CAPACITY) {
                throw new CityAtCapacityException(owner.getCity());
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
