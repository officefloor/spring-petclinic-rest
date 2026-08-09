package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the new owner's {@code namesakeCount}: the number of existing owners that, at the moment
 * this owner is created, already share the same first name and last name, compared
 * case-insensitively. The value is a snapshot taken before the new row is saved, so the new owner is
 * never counted against itself and the stored figure stays stable as later owners are added.
 *
 * <p>Runs after {@link BuildOwner} (so the entity and its names exist) and before {@link SaveOwner},
 * mutating the not-yet-persisted owner in place.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (equalsIgnoreCase(firstName, existing.getFirstName())
                    && equalsIgnoreCase(lastName, existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
