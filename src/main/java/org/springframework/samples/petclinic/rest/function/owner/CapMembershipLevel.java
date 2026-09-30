package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps the new owner's {@code membershipLevel} at one above the current maximum
 * membershipLevel among their existing household members (those sharing the owner's
 * {@code householdId} — see {@link OwnerHousehold}). Runs after {@link AssignMembership}
 * has derived the level and before the owner is saved, so the scan of existing members
 * excludes the owner being created.
 *
 * <p>When the household has no existing member with a level, no cap applies and the
 * derived level stands. Otherwise the level is lowered to {@code max + 1} whenever the
 * derived level would exceed it; a level already at or below the ceiling is left as is.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Integer level = owner.getMembershipLevel();
        if (level == null) {
            return;
        }
        Integer maxExisting = OwnerHousehold.maxMembershipLevel(owner.getHouseholdId(),
                ownerRepository.findAll());
        if (maxExisting != null && level > maxExisting + 1) {
            owner.setMembershipLevel(maxExisting + 1);
        }
    }
}
