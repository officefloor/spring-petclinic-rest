package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already holds {@link #CAPACITY} or more owners.
 * The request's city is compared with each stored owner's city case-insensitively, with
 * surrounding whitespace trimmed, so "Sydney" and " sydney " count towards the same city.
 * Runs before {@link BuildOwner} (it reads the request DTO, not the built entity) and before
 * {@link SaveOwner} (so the request itself is not counted). On reaching the cap raises
 * {@link CityAtCapacityException} (409).
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners permitted per city; the request is rejected at this count. */
    public static final int CAPACITY = 50;

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
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }

    /** Lower-case and trim, treating null as empty, for case-insensitive city comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
