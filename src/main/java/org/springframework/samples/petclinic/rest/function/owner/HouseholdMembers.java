package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single definition of "household member" shared by the steps that reason over a household:
 * {@link DeriveOwnerHouseholdSize}, {@link CapOwnerMembershipLevel} and
 * {@link DetectOwnerPossibleDuplicate}. A member is any <em>other</em> non-deleted owner that shares
 * the given owner's {@link Owner#getHouseholdId() householdId}. Keeping the self-exclusion and
 * soft-delete rules in one place ensures the consumers stay in lockstep rather than re-encoding the
 * filters independently.
 */
final class HouseholdMembers {

    private HouseholdMembers() {
    }

    /**
     * The non-deleted owners, other than {@code owner} itself, that share its
     * {@link Owner#getHouseholdId() householdId}. Empty when the owner has no household id. The owner
     * itself is excluded by id, so the result is correct in the create pipeline (where the owner may
     * not yet be persisted and so never appears in {@link OwnerRepository#findAll()}) as well as on
     * reads.
     */
    static List<Owner> of(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return List.of();
        }
        List<Owner> members = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // the owner itself is not one of its household members
            }
            if (existing.isDeleted()) {
                continue; // soft-deleted owners are not household members
            }
            if (householdId.equals(existing.getHouseholdId())) {
                members.add(existing);
            }
        }
        return members;
    }
}
