package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft duplicate: a new owner that passed the duplicate block (see
 * {@link EnsureUniqueIdentity}) but still shares an existing owner's
 * {@link HouseholdNormalizer#id(String, String) household id} (same last name and postcode) while
 * giving a different telephone. Such a request is allowed but marked for follow-up: the built
 * {@link Owner} records {@code possibleDuplicate} true and {@code possibleDuplicateOf} the matching
 * owner's id (the earliest match when several exist). A <em>declared</em> household member (the
 * request set {@code sharesHousehold}) is never flagged — deliberately sharing a household is not a
 * suspected duplicate. When nothing matches, {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} stays absent. Runs before {@link SaveOwner}, so the new owner is not
 * yet persisted and is never matched against itself. Mutates the built {@link Owner} in place.
 */
public class FlagPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = owner.getHouseholdId();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (HouseholdNormalizer.belongsTo(existing, householdId)
                    && !owner.getTelephone().equals(existing.getTelephone())) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
