package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner} (so the owner's city is set) and before
 * {@link SaveOwner}. Counts the existing owners whose city matches this owner's city
 * (case-insensitively) - the same per-city bucket the {@link EnsureCityCapacity} rule guards - and
 * stamps a capacity warning onto the built owner in place: true when that city already holds between
 * 40 and 49 owners (approaching the capacity limit of 50), otherwise false. As it runs before the
 * owner is saved, the count excludes the owner being created; the hard rejection at 50 has already
 * fired earlier in the pipeline, so this never sees a full city. The evaluated flag is fixed at
 * creation and returned unchanged on later reads.
 */
public class AssignCapacityWarning {

    private static final int WARNING_THRESHOLD = 40;

    private static final int CAPACITY = 50;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> city.equalsIgnoreCase(existing.getCity()))
                .count();
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CAPACITY);
    }
}
