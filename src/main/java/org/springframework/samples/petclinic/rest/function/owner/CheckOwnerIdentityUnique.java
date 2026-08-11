package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create-owner request that duplicates an existing owner. Two rules are folded here,
 * both keyed off the now-computed {@code householdId} (SHA-256 of last name + postcode, see
 * {@link AssignHousehold}):
 *
 * <ol>
 * <li><b>Identity match</b> — the derived {@code identityKey}
 * ({@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, see
 * {@link IdentityKey}) exactly equals an existing owner's. This expresses the former telephone
 * and email duplicate checks. It fires regardless of {@code sharesHousehold}, so a full
 * duplicate that opts in is still a 409.</li>
 * <li><b>Household duplicate</b> — an existing owner shares this owner's {@code householdId}
 * (i.e. the same last name and postcode). Because the household is keyed on those fields, a
 * second such owner is the same household and is rejected — <em>unless</em> the request sets
 * {@code sharesHousehold}, which admits it as a declared household member.</li>
 * </ol>
 *
 * <p>Runs after {@link AssignHousehold} (so the built owner's householdId is settled) and
 * before {@link CheckPossibleDuplicate}/{@link SaveOwner} (so the new owner is not yet among
 * {@code findAll()}). On a match raises {@link DuplicateIdentityException} (409).
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val Owner built, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String identityKey = IdentityKey.of(built);
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String householdId = built.getHouseholdId();
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a duplicate
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
            if (!sharesHousehold && householdId != null
                    && householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
