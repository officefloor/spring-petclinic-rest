package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check for create-owner, responding 409 via
 * {@link DuplicateOwnerException} for either kind of duplicate:
 *
 * <ul>
 * <li><b>Household duplicate</b> — an existing owner shares the request's deterministic
 * {@code householdId} (same last name and postcode, see {@link OwnerHouseholds}). Rejected
 * unless the request opts in with {@code sharesHousehold}, which declares it a household member
 * and bypasses this block.</li>
 * <li><b>Identity duplicate</b> — an existing owner has the same derived {@code identityKey}
 * (telephone, email and household, see {@link OwnerIdentities}); this always rejects, so even a
 * declared member cannot be an exact resubmission of an existing owner.</li>
 * </ul>
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail} so the request
 * telephone is already E.164 and its email lower-cased. The household component here matches the
 * {@code householdId} that {@link AssignOwnerHousehold} later stores on the owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        String householdId = OwnerHouseholds.id(request.getLastName(), request.getPostcode());
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        boolean checkHousehold = !sharesHousehold && hasText(request.getPostcode());
        String identityKey = OwnerIdentities.key(request.getTelephone(), request.getEmail(), householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a new one
            }
            if (checkHousehold && householdId.equals(OwnerHouseholds.of(existing))) {
                throw new DuplicateOwnerException(householdId);
            }
            if (identityKey.equals(OwnerIdentities.of(existing))) {
                throw new DuplicateOwnerException(identityKey);
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
