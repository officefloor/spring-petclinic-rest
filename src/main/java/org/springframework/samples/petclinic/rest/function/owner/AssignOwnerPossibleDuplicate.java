package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate. A new owner that reaches this step was not rejected by the identity
 * check ({@link EnsureUniqueIdentity}) — no live owner repeats its {@link Owner#getIdentityKey()
 * identity key}. A declared member ({@code sharesHousehold}) is a genuine household member, not a
 * suspected duplicate, so it is never flagged here. Any other owner that shares an existing live
 * owner's postcode and a phonetically-equal last name ({@link Identities}) while differing in
 * identity key is created anyway and marked as a possible duplicate of that owner. Runs before
 * {@link SaveOwner} persists the flags; when several match, the earliest (lowest id) is recorded.
 * An owner with no postcode can never be a possible duplicate.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        Identities.softMatchesOf(ownerRepository, owner).stream()
                .min(Comparator.comparingInt(Owner::getId))
                .ifPresent(match -> {
                    owner.setPossibleDuplicate(true);
                    owner.setPossibleDuplicateOf(match.getId());
                });
    }
}
