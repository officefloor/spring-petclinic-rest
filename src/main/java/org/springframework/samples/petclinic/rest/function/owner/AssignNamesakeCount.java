package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records, on the owner being created, how many owners already exist that share the same
 * firstName and lastName (compared case-insensitively). The count is a snapshot taken
 * before this owner is saved, so it counts only the pre-existing namesakes. Runs before
 * {@link SaveOwner}.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalize(owner.getFirstName());
        String lastName = normalize(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (normalize(existing.getFirstName()).equals(firstName)
                    && normalize(existing.getLastName()).equals(lastName)) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
