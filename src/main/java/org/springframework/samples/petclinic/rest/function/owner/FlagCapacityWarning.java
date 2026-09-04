package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.CityAtCapacityException;

/**
 * Flags a create-owner request as approaching its city's capacity limit. Runs before
 * {@link SaveOwner}, so {@link OwnerRepository#findAll()} returns only the owners that predate this
 * create. It counts those already registered in this owner's city (compared case-insensitively and
 * trimmed, mirroring {@link EnsureCityCapacity}) and sets {@code capacityWarning} true when that
 * count is between {@link #WARNING_THRESHOLD} and one below the hard
 * {@link CityAtCapacityException#CAPACITY} limit (40-49); otherwise false. A city already at
 * capacity is rejected earlier by {@link EnsureCityCapacity}, so this step never sees 50 or more.
 */
public class FlagCapacityWarning {

    /** A city already holding at least this many owners warns the next create. */
    public static final int WARNING_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (city.equals(normalize(existing.getCity()))) {
                count++;
            }
        }
        owner.setCapacityWarning(count >= WARNING_THRESHOLD && count < CityAtCapacityException.CAPACITY);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
