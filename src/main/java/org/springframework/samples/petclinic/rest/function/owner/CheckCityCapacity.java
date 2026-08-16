package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects creating an owner when the owner's city already contains {@value #MAX_PER_CITY} or more
 * owners, so the create endpoint responds 409 instead of overfilling the city. City names are
 * compared case-insensitively with surrounding whitespace trimmed. Runs after {@link ValidateOwner}
 * (which publishes the body) and before {@link BuildOwner}, counting every existing owner.
 */
public class CheckCityCapacity {

    /** Maximum owners permitted per city; the next create in a full city is rejected. */
    static final int MAX_PER_CITY = 50;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = normalize(request.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        if (count >= MAX_PER_CITY) {
            throw new CityAtCapacityException(request.getCity(), count);
        }
    }

    /** Trims and lower-cases so comparison ignores case and surrounding spacing. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
