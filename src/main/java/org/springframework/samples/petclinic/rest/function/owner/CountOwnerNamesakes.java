package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners — before this create —
 * whose firstName and lastName both match the new owner's, compared case-insensitively. Runs after
 * {@link BuildOwner} has created the entity and before {@link SaveOwner} persists it, so the count
 * reflects only pre-existing owners (the new owner has no id yet and is skipped). The count is
 * stored on the built owner so the saved and returned record carries it.
 */
public class CountOwnerNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (firstName != null && lastName != null
                    && firstName.equalsIgnoreCase(existing.getFirstName())
                    && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount((int) count);
    }
}
