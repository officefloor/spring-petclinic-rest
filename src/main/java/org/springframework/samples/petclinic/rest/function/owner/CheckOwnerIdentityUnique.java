package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * The duplicate block for a create-owner request. It rejects a create with a 409 only for an
 * <b>exact identity duplicate</b> — an existing owner with the same whole
 * {@link OwnerIdentityKey identity key} ({@code normalizedTelephone + '|' + email + '|' +
 * householdId}, keyed off the deterministic {@code householdId} resolved by
 * {@link DetermineOwnerHousehold} from {@code (lastName, postcode)}). This is always rejected,
 * even for a declared household member.
 *
 * <p>Merely sharing a household (same last name and postcode) with an existing owner is
 * <em>not</em> a hard duplicate: a genuine second member with a different telephone or email has a
 * distinct identity key and is allowed. Such a member is instead surfaced as a soft
 * {@code possibleDuplicate} by {@link FlagOwnerPossibleDuplicate} unless the request declares the
 * shared household with {@code sharesHousehold} true.
 */
public class CheckOwnerIdentityUnique {

    public void service(@Val OwnerFieldsDto request, @Val HouseholdId householdId,
            OwnerRepository ownerRepository) throws DuplicateOwnerIdentityException {
        String identityKey = OwnerIdentityKey.of(request.getTelephone(), request.getEmail(),
                householdId.value());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner never blocks a create
            }
            if (identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
                throw new DuplicateOwnerIdentityException(identityKey);
            }
        }
    }
}
