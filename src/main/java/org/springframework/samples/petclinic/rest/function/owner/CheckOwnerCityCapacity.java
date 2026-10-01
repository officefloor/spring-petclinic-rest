package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
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
 *
 * <p>The count of existing owners in the city is published as the {@link CityOwnerCount}-qualified
 * variable and reused later by {@link CheckOwnerCityCapacityWarning} for the approaching-capacity
 * warning, so the two share one scan and one count and cannot disagree.
 */
public class CheckOwnerCityCapacity {

    /** A city may hold at most this many owners; the create of one more is rejected. */
    static final int MAX_OWNERS_PER_CITY = 50;

    /**
     * At or above this many existing owners in the city (but still below {@link #MAX_OWNERS_PER_CITY}),
     * the create is allowed but flagged as approaching capacity.
     */
    static final int WARN_OWNERS_PER_CITY = 40;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository,
            @CityOwnerCount Out<Integer> cityOwnerCount) throws OwnerCityCapacityException {
        String city = request.getCity();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsNormalized(city, existing.getCity())) {
                count++;
            }
        }
        cityOwnerCount.set(count);
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
