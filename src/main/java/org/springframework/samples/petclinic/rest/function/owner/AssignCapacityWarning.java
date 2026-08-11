package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code capacityWarning}: {@code true} when the owner's city already held
 * between 40 and 49 owners (inclusive) — approaching the per-city capacity limit of 50 — otherwise
 * {@code false}. The city is compared case-insensitively, matching {@link CheckOwnerCityCapacity}
 * which enforces the hard rejection at 50. Runs before the new owner is saved, so the count reflects
 * only the owners that existed before this create.
 */
public class AssignCapacityWarning {

    private static final long WARNING_LOWER = 40;
    private static final long WARNING_UPPER = 49;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCapacityWarning(inCity >= WARNING_LOWER && inCity <= WARNING_UPPER);
    }
}
