package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The duplicate check, rejecting a create-owner request with a {@link DuplicateOwnerException} (409)
 * on either of two grounds:
 *
 * <ul>
 * <li><b>Full identity.</b> The whole derived {@link OwnerIdentityKey}
 * ({@code telephone|email|householdId}) equals an existing owner's. This always blocks, even when
 * the request opts in via {@code sharesHousehold}.</li>
 * <li><b>Household.</b> The computed {@code householdId} (see {@link AssignHousehold}, keyed on
 * last name and postcode) equals an existing owner's. Because the household is now keyed on
 * (last name, postcode), a second owner sharing those is the same household and is blocked — unless
 * the request sets {@code sharesHousehold}, which declares the new owner an intentional household
 * member and waves this ground through (the full-identity ground still applies).</li>
 * </ul>
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner's telephone, email
 * and {@code householdId} are all in their stored, computed form, and before {@link SaveOwner} so
 * the not-yet-persisted new owner is compared only against existing owners.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String identityKey = OwnerIdentityKey.of(owner);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        // An owner who supplies their own email is a distinguishable individual and may join an
        // existing household as a member (subject to the full-identity ground below); without an
        // email a same-household owner cannot be told apart and is still blocked as a duplicate.
        boolean hasDistinguishingEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        String householdId = owner.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never a duplicate of itself
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is ignored by the duplicate check
            }
            if (identityKey.equals(OwnerIdentityKey.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
            if (!sharesHousehold && !hasDistinguishingEmail && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateOwnerException("household " + householdId);
            }
        }
    }
}
