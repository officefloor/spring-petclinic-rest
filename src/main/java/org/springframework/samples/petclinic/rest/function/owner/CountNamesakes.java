package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already shared the new owner's name at the moment it was created.
 * Runs before {@link SaveOwner}, so {@link OwnerRepository#findAll()} returns only the owners that
 * predate this create. It counts those whose firstName and lastName both match the new owner's,
 * compared case-insensitively, and stores the total as {@code namesakeCount}; a unique name yields 0.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(owner.getFirstName(), existing.getFirstName())
                    && equalsIgnoreCase(owner.getLastName(), existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
