package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning} — {@code true} when the owner's city already holds
 * between {@code WARN_THRESHOLD} (40) and {@code CAPACITY - 1} (49) owners at the time of this
 * create (approaching the hard capacity limit of 50), otherwise {@code false}. City is compared
 * case-insensitively, matching {@link RequireCityCapacity}. Runs before {@link SaveOwner} so the
 * count reflects existing owners only; the hard rejection at {@code CAPACITY} has already run in
 * {@link RequireCityCapacity}, so a warning owner is never itself rejected.
 */
public class AssignOwnerCapacityWarning {

    private static final int WARN_THRESHOLD = 40;

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = city == null ? 0 : ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCapacityWarning(count >= WARN_THRESHOLD && count < CAPACITY);
    }
}
