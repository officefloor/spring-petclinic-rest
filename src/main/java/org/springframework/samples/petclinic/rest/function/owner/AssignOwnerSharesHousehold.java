package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner its household-sharing flag: {@code true} when another
 * owner already has the same address and city at the moment the owner is created,
 * otherwise {@code false}. The owner is still created either way.
 */
public class AssignOwnerSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean sharesHousehold = false;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(owner, existing)) {
                continue;
            }
            if (Objects.equals(owner.getAddress(), existing.getAddress())
                    && Objects.equals(owner.getCity(), existing.getCity())) {
                sharesHousehold = true;
                break;
            }
        }
        owner.setSharesHousehold(sharesHousehold);
    }

    private static boolean isSameOwner(Owner a, Owner b) {
        return a.getId() != null && Objects.equals(a.getId(), b.getId());
    }
}
