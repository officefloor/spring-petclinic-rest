package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after the owner is built but before it is saved. Sets the owner's {@code namesakeCount} to the
 * number of existing owners (those already persisted, so excluding this new one) that share the same
 * firstName and lastName, compared case-insensitively. The value is mutated in place via {@code @Val}
 * for the save/respond steps to persist and return.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> equalsIgnoreCase(firstName, existing.getFirstName())
                        && equalsIgnoreCase(lastName, existing.getLastName()))
                .count();
        owner.setNamesakeCount((int) count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
