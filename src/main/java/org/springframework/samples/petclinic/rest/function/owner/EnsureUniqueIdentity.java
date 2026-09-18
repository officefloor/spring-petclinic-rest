package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Create-owner step: the duplicate block. Two owners are the same person when they share the
 * same {@link IdentityKey} (normalized telephone, email and household), so a request whose key
 * already belongs to an existing owner is rejected with a 409. Additional members of the same
 * {@link Household} with a distinct identity (a different telephone or email) are <em>not</em>
 * duplicates and are admitted; {@code sharesHousehold} still bypasses the block explicitly.
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner already carries its final
 * {@code householdId}, part of its identity key) and before {@link FlagPossibleDuplicate} and
 * {@link SaveOwner}.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member bypasses the duplicate block
        }
        if (owner.getHouseholdId() == null) {
            return; // no postcode -> no household -> nothing to collide with
        }
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer occupies its identity
            }
            if (identityKey.equals(IdentityKey.of(existing))) {
                throw new DuplicateIdentityException(identityKey);
            }
        }
    }
}
