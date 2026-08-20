package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate block for a create-owner request. It rejects a create with a 409 in two cases,
 * both keyed off the deterministic {@code householdId} resolved by {@link DetermineOwnerHousehold}
 * (derived from {@code (lastName, postcode)}):
 *
 * <ul>
 * <li><b>Exact identity duplicate</b> — an existing owner has the same whole
 * {@link OwnerIdentityKey identity key} ({@code normalizedTelephone + '|' + email + '|' +
 * householdId}). This is always rejected, even for a declared household member.</li>
 * <li><b>Household duplicate</b> — an existing owner shares the same {@code householdId} (i.e. the
 * same last name and postcode). Because the household is keyed on {@code (lastName, postcode)},
 * such an owner is a second member of an existing household and is rejected — <em>unless</em> the
 * request opts in with {@code sharesHousehold} true, which bypasses this block so the owner is
 * created as a declared household member.</li>
 * </ul>
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, @Val HouseholdId householdId,
            OwnerRepository ownerRepository) throws DuplicateOwnerIdentityException {
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String household = householdId.value();
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(),
                household);
        for (Owner existing : ownerRepository.findAll()) {
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
            if (!sharesHousehold && household.equals(existing.getHouseholdId())) {
                throw new DuplicateOwnerIdentityException(household);
            }
        }
    }
}
