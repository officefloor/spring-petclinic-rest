package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate. A new owner that reaches this step is not blocked by the
 * household duplicate check ({@link EnsureUniqueHousehold}) — either it has no existing
 * household member, or it opted in with {@code sharesHousehold}. A declared member is a
 * genuine household member, not a suspected duplicate, so it is never flagged here. Any
 * other owner that shares an existing owner's household (same last name and postcode, hence
 * the same {@code householdId}, see {@link Households}) but differs in telephone is created
 * anyway and marked as a possible duplicate of that owner. Runs before {@link SaveOwner}
 * persists the flags; when several match, the earliest (lowest id) is recorded. An owner
 * with no postcode has no household and can never be a possible duplicate.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (owner.getHouseholdId() == null || Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        Households.membersOf(ownerRepository, owner.getLastName(), owner.getPostcode()).stream()
                .filter(existing -> !owner.getTelephone().equals(existing.getTelephone()))
                .min(Comparator.comparingInt(Owner::getId))
                .ifPresent(match -> {
                    owner.setPossibleDuplicate(true);
                    owner.setPossibleDuplicateOf(match.getId());
                });
    }
}
