package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already share this owner's firstName and lastName
 * (compared case-insensitively) at creation time, before {@link SaveOwner} runs. The
 * new owner is not yet persisted, so {@code findAll()} sees only prior owners.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalize(owner.getFirstName());
        String lastName = normalize(owner.getLastName());
        int count = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (firstName.equals(normalize(other.getFirstName()))
                    && lastName.equals(normalize(other.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
