package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that already share this
 * owner's first and last name, compared case-insensitively.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline before {@link SaveOwner}, so the new owner is not
 * yet persisted and therefore not counted among its own namesakes. The value is frozen at creation
 * time and returned unchanged on later reads.
 */
public class CountOwnerNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (equalsIgnoreCase(existing.getFirstName(), firstName)
                    && equalsIgnoreCase(existing.getLastName(), lastName)) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
