package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps, on the owner being created, its {@code membershipLevel} so it cannot exceed one above the
 * current maximum {@code membershipLevel} among the existing members of its household — the owners
 * already saved that share the same deterministic {@code householdId} (see {@link OwnerIdentityKey}).
 * With no existing household member no cap applies and the level assigned by
 * {@link AssignMembershipLevel} stands.
 *
 * <p>Runs after {@link AssignMembershipLevel}, which has set the raw level, and before
 * {@link SaveOwner} persists it.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }
        String householdId = owner.getHouseholdId();
        Integer maxExisting = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(OwnerIdentityKey.householdIdOf(existing))
                    && existing.getMembershipLevel() != null
                    && (maxExisting == null || existing.getMembershipLevel() > maxExisting)) {
                maxExisting = existing.getMembershipLevel();
            }
        }
        if (maxExisting != null && level > maxExisting + 1) {
            owner.setMembershipLevel(maxExisting + 1);
        }
    }
}
