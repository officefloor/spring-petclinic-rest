package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners share this owner's firstName and lastName
 * (case-insensitively) at create time, before the new owner is saved.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalise(owner.getFirstName());
        String lastName = normalise(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(normalise(existing.getFirstName()))
                    && lastName.equals(normalise(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
