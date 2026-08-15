package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags an owner whose city is approaching the per-city
 * capacity limit enforced by {@link RejectCityAtCapacity}. When the owner's city already contains
 * between {@value #WARNING_THRESHOLD} and {@value #MAX_OWNERS_PER_CITY} minus one owners (inclusive),
 * the new owner is stamped with {@code capacityWarning} true; otherwise false. A create is still
 * hard-rejected once the city holds {@value #MAX_OWNERS_PER_CITY} owners, so the warning band is the
 * final stretch below that limit. City matching is case-insensitive, matching the rejection rule.
 *
 * <p>Runs after {@link BuildOwner} so the owner exists to stamp, and before {@link SaveOwner} so the
 * count reflects only owners already persisted, never this owner itself. {@code @Val} yields the
 * built owner, mutated in place and persisted by the save step.
 */
public class AssignCapacityWarning {

    /** The new owner warns once its city already holds at least this many owners. */
    static final int WARNING_THRESHOLD = 40;

    /** The per-city hard limit; a city at this count rejects rather than warns. */
    static final int MAX_OWNERS_PER_CITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .map(Owner::getCity)
                .filter(existing -> existing != null && existing.equalsIgnoreCase(city))
                .count();
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < MAX_OWNERS_PER_CITY);
    }
}
