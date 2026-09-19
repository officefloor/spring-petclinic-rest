package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@code membershipLevel} at one above the highest level currently held
 * by an existing member of its household (same last name and postcode, see
 * {@link Households}). With no existing household member no cap applies. Runs after
 * {@link BuildOwner} (so the entity, its name and postcode exist) and before
 * {@link SaveOwner} persists the cap.
 */
public class AssignOwnerMembershipLevelCap {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Households.maxMembershipLevel(ownerRepository, owner.getLastName(), owner.getPostcode())
                .ifPresent(maxLevel -> owner.setMembershipLevelCap(maxLevel + 1));
    }
}
