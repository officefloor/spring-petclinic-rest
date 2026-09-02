package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a city approaching its capacity limit: {@code true} when 40 to 49 other owners already
 * share this owner's city (case-insensitive after trimming), i.e. approaching the hard limit of 50
 * at which {@link RejectOwnerCityAtCapacity} rejects; otherwise {@code false}. The owner itself is
 * excluded by id.
 */
public final class CapacityWarning {

    private static final int WARN_FROM = 40;

    private static final int WARN_TO = 49;

    private CapacityWarning() {
    }

    public static boolean of(Owner owner, OwnerRepository repository) {
        String city = normalize(owner.getCity());
        int count = 0;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId()) && city.equals(normalize(other.getCity()))
                    && ++count > WARN_TO) {
                return false;
            }
        }
        return count >= WARN_FROM;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
