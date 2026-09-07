package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Rejects a create-owner request whose city already contains {@value #CAPACITY} or more owners,
 * with a 409 Conflict. City is compared case-insensitively (matching the customer-code rule).
 * Runs after {@link BuildOwner} and before the owner is saved, so the count reflects only
 * pre-existing owners.
 *
 * <p>When the pre-existing count is between {@value #WARNING_THRESHOLD} and {@value #CAPACITY}
 * minus one (i.e. approaching but not yet at the limit), the owner is stamped with
 * {@code capacityWarning} true via {@link Owner#setCapacityWarning(boolean)}; it is returned as
 * {@code capacityWarning}. The hard rejection at {@value #CAPACITY} is unchanged.
 */
public class CheckOwnerCityCapacity {

    /** Maximum number of owners a single city may hold. */
    private static final int CAPACITY = 50;

    /** Pre-existing owners at or above this (but below {@link #CAPACITY}) trigger the warning. */
    private static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws CityAtCapacityException {
        String city = owner.getCity();
        if (city == null) {
            return;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save), not counted against the city
            }
            if (city.equalsIgnoreCase(existing.getCity())) {
                count++;
            }
        }
        if (count >= CAPACITY) {
            throw new CityAtCapacityException(city, count);
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD);
    }
}
