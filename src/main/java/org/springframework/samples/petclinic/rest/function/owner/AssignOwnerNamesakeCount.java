package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount} — the number of existing owners (before
 * this create) sharing the same {@code firstName} and {@code lastName}, compared
 * case-insensitively.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> equalsIgnoreCase(existing.getFirstName(), firstName)
                        && equalsIgnoreCase(existing.getLastName(), lastName))
                .count();
        owner.setNamesakeCount((int) count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
