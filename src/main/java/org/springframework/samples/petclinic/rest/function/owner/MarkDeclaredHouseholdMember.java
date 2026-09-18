package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Marks a new owner joining an already-occupied household as a <em>declared</em> member when the
 * request opts in with {@code sharesHousehold=true}. A household is keyed on (last name, postcode) via
 * the deterministic {@link Owner#getHouseholdId() householdId}, so additional members are admitted
 * — a second owner is not a duplicate as long as its identity (telephone) differs, which
 * {@link EnsureUniqueIdentity} enforces. What differs is intent: a declared member is deliberate, so
 * this step publishes {@link DeclaredHouseholdMember} to tell {@link DetectPossibleDuplicate} to leave
 * it unflagged. An undeclared owner joining an occupied household is admitted too, but stays a
 * suspected soft duplicate.
 *
 * <p>Runs after {@link AssignHouseholdId} so the owner carries its final id, and before
 * {@link DetectPossibleDuplicate}. An owner without a postcode belongs to no household, so nothing is
 * marked.
 */
public class MarkDeclaredHouseholdMember {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository,
            Out<DeclaredHouseholdMember> declaredMember) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        boolean occupied = Household.existingMembers(owner, ownerRepository).stream()
                .anyMatch(existing -> !existing.isDeleted());
        if (occupied) {
            declaredMember.set(DeclaredHouseholdMember.INSTANCE);
        }
    }
}
