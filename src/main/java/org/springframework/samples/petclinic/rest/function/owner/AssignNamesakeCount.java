package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code namesakeCount}: the number of
 * existing owners (before this create) sharing the same {@code firstName} and {@code lastName},
 * compared case-insensitively.
 *
 * <p>Runs before {@link SaveOwner}, so the owner being created is not yet persisted and therefore
 * never counts itself.
 */
public class AssignNamesakeCount {

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
