package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner its namesake count: the number of other owners that
 * already share the same last name at the moment the owner is created.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int namesakes = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (Objects.equals(owner.getLastName(), existing.getLastName())) {
                namesakes++;
            }
        }
        owner.setNamesakeCount(namesakes);
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }
}
