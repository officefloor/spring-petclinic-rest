package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Runs after {@link ResolveHousehold} (which derives the deterministic {@link Household.Id} from
 * the request's last name and postcode) and before {@link BuildOwner}. The duplicate-detection
 * gate: because a household is now keyed on {@code (lastName, postcode)}, owners sharing a last
 * name and postcode are the same household, so a second owner in an existing household is a
 * duplicate and is rejected with a 409.
 *
 * <p>The write-only {@code sharesHousehold} flag only bypasses this block: when set, the new
 * owner is created as a declared household member instead of being rejected. Publishes nothing;
 * it only escalates on conflict.
 */
public class ValidateUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerRepository ownerRepository) throws DuplicateIdentityException {
        if (householdId == null || !householdId.isPresent()) {
            // No postcode means no household, so there is nothing to collide with.
            return;
        }
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            // Declared household member: bypass the duplicate block.
            return;
        }
        // No self to exclude yet: the new owner is not persisted at this point.
        if (Household.hasMember(householdId.value(), null, ownerRepository)) {
            throw new DuplicateIdentityException("Household " + householdId.value()
                    + " already has an owner (same last name and postcode)");
        }
    }
}
