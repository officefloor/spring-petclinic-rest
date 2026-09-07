package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code capacityWarning}: {@code true}
 * when the owner's city already contained between 40 and 49 owners (inclusive) before this owner was
 * created, approaching the hard capacity limit of 50 enforced by {@link RequireCityCapacity};
 * otherwise {@code false}. Cities are compared the same way as {@link RequireCityCapacity},
 * case-insensitively with trimmed whitespace. Runs after {@link BuildOwner} and before
 * {@link SaveOwner} so the owner being created is not itself counted.
 */
public class FlagCapacityWarning {

    /** Lower bound (inclusive) of the "approaching capacity" band. */
    private static final int WARNING_THRESHOLD = 40;

    /** Hard capacity limit; a city with this many owners is rejected by {@link RequireCityCapacity}. */
    private static final int CITY_CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CITY_CAPACITY);
    }

    private static String normalize(String city) {
        return city == null ? "" : city.trim().toLowerCase();
    }
}
