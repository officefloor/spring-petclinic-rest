package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerCityCapacityException;

/**
 * Runs after {@link ValidateOwner} and before {@link BuildOwner}. Rejects the create with a 409 via
 * {@link OwnerCityCapacityException} when the owner's city already contains the maximum number of
 * owners ({@value #MAX_OWNERS_PER_CITY}); the city is compared case-insensitively with surrounding
 * whitespace trimmed. The new owner is still unsaved, so it is not counted against the limit.
 */
public class CheckOwnerCityCapacity {

    /** A city may hold at most this many owners; the create of one more is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws OwnerCityCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsNormalized(city, existing.getCity())) {
                count++;
            }
        }
        if (count >= MAX_OWNERS_PER_CITY) {
            throw new OwnerCityCapacityException(city, count);
        }
    }

    private static boolean equalsNormalized(String a, String b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
