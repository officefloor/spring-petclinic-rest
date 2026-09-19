package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code namesakeCount}: the number of existing owners that share
 * the new owner's first and last name, compared case-insensitively. Runs after
 * {@link BuildOwner} (so the entity and its name exist) and before {@link SaveOwner}
 * persists the count, so the freshly created owner is not counted among its own
 * namesakes.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findByLastName(owner.getLastName()).stream()
                .filter(existing -> equalsIgnoreCase(existing.getFirstName(), owner.getFirstName())
                        && equalsIgnoreCase(existing.getLastName(), owner.getLastName()))
                .count();
        owner.setNamesakeCount((int) count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
