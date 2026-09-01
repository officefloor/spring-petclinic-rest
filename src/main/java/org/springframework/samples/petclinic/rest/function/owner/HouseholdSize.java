package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts the members of an owner's household: the owners sharing this owner's last name and address,
 * compared case-insensitively with collapsed whitespace (the same grouping
 * {@link RejectDuplicateOwnerHousehold} uses). The owner itself is included, so a freshly created
 * owner counts towards its own household total.
 */
public final class HouseholdSize {

    private HouseholdSize() {
    }

    public static int of(Owner owner, OwnerRepository repository) {
        String lastName = normalize(owner.getLastName());
        String address = normalize(owner.getAddress());
        int count = 0;
        for (Owner other : repository.findAll()) {
            if (lastName.equals(normalize(other.getLastName()))
                    && address.equals(normalize(other.getAddress()))) {
                count++;
            }
        }
        return count;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
