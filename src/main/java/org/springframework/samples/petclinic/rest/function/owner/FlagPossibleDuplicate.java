package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether the new owner is a suspected soft duplicate. Duplicates are caught in layers: a
 * hard duplicate (same {@link OwnerIdentity identity key}) is rejected by
 * {@link EnsureUniqueIdentity}, and a household collision (an existing owner sharing this owner's
 * {@link HouseholdKey household id} — same last name and postcode) is rejected as a 409 by
 * {@link EnsureUniqueHousehold}. This step marks the survivors: it sets {@code possibleDuplicate}
 * to whether an existing owner shares this owner's household id, with {@code possibleDuplicateOf}
 * set to that owner's id.
 *
 * <p>A declared household member ({@code sharesHousehold}) knowingly joins the household, so it is
 * not a suspected duplicate and stays {@code possibleDuplicate = false}.
 *
 * <p>Runs after {@link AssignHouseholdId} has set the household id and before {@link SaveOwner}
 * persists the entity, so {@code findAll()} sees only the owners that existed before this create.
 * The flags are stored on the owner and returned on every later read.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
