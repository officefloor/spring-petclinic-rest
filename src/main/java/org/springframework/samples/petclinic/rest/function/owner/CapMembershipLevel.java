package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Caps a new owner's membership level at one above the highest level among their existing household
 * members (same householdId, not deleted). With no such member the computed level stands, so no cap is
 * stored. Runs after {@link CountNamesakes} so the computed level is final, and before Save so the cap
 * persists and later reads see it.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        Integer highest = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue;
            }
            if (householdId != null && householdId.equals(existing.getHouseholdId())) {
                int level = effectiveLevel(existing);
                if (highest == null || level > highest) {
                    highest = level;
                }
            }
        }
        if (highest != null) {
            owner.setMembershipLevel(Math.min(OwnerMembershipLevel.of(owner), highest + 1));
        }
    }

    /** An existing member's effective level: its stored cap when present, otherwise its computed level. */
    private static int effectiveLevel(Owner owner) {
        return owner.getMembershipLevel() != null ? owner.getMembershipLevel() : OwnerMembershipLevel.of(owner);
    }
}
