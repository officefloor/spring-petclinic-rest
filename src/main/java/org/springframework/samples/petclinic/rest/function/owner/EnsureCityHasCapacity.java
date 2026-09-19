package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create request whose city already holds {@link #CAPACITY} or more owners, so the
 * request maps to a 409. Existing owners are counted case-insensitively on their trimmed city,
 * so incidental formatting differences neither hide nor inflate the count. Runs among the other
 * create guards, before {@link BuildOwner} maps the request to an entity.
 */
public class EnsureCityHasCapacity {

    /** Maximum number of owners allowed to share a single city. */
    static final int CAPACITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(request.getCity(), CAPACITY);
        }
    }

    private static String normalize(String city) {
        return city == null ? "" : city.trim().toLowerCase();
    }
}
