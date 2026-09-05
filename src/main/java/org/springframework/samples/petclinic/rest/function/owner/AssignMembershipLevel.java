package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code membershipLevel}: the level derived from its membership points
 * (see {@link OwnerMapper#deriveMembershipLevel(Owner)}), but <em>capped</em> so it can never
 * exceed one above the current maximum membership level among the owner's existing household
 * members (owners sharing the same non-null {@code householdId}, excluding soft-deleted ones).
 * With no existing household member — a lone household, or an owner not in a shared household —
 * no cap applies and the derived level stands.
 *
 * <p>Runs after {@link AssignHouseholdSize} (so {@code householdId} and {@code householdSize},
 * which feeds the point total, are settled) but before {@link SaveOwner} (so the owner being
 * created is not yet persisted and cannot cap itself), mutating the owner in place so the
 * persisted value — and every later read — carries the capped level.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper) {
        int level = ownerMapper.deriveMembershipLevel(owner);
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            Integer maxMemberLevel = null;
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue; // never cap against itself
                }
                if (existing.isDeleted()) {
                    continue; // a soft-deleted owner no longer counts toward the household
                }
                if (!householdId.equals(existing.getHouseholdId())) {
                    continue;
                }
                int memberLevel = ownerMapper.toMembershipLevel(existing);
                if (maxMemberLevel == null || memberLevel > maxMemberLevel) {
                    maxMemberLevel = memberLevel;
                }
            }
            if (maxMemberLevel != null) {
                level = Math.min(level, maxMemberLevel + 1);
            }
        }
        owner.setMembershipLevel(level);
    }
}
