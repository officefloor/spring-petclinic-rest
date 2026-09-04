package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Capacity warning: an owner carries a warning once its city (compared case-insensitively) already
 * holds between {@value #WARN_FLOOR} and {@value #WARN_CEILING} owners, approaching the hard limit
 * of 50 enforced by {@link EnsureCityCapacity}. Mirrors that rule's case-insensitive city count.
 */
public final class CapacityWarning {

    private static final int WARN_FLOOR = 40;

    private static final int WARN_CEILING = 49;

    private CapacityWarning() {
    }

    public static boolean warned(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
            .filter(other -> city != null && city.equalsIgnoreCase(other.getCity()))
            .count();
        return inCity >= WARN_FLOOR && inCity <= WARN_CEILING;
    }
}
