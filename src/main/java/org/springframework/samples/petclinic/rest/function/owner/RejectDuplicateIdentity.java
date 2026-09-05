package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects a create-owner request whose {@link OwnerIdentity#key(Owner) identity key} already
 * belongs to an existing owner. The key consolidates all duplicate detection — it is
 * {@code normalizedTelephone + '|' + email + '|' + householdId} — so a new owner collides only
 * when its telephone, email <em>and</em> household all match an existing owner's. Because the
 * telephone is part of the key, two members of the same household (same {@code householdId},
 * derived deterministically from last name and postcode, see {@link AssignHousehold}) with
 * different telephones have different keys and are both allowed; the later
 * {@link AssignPossibleDuplicate} step merely flags the second as a soft duplicate.
 *
 * <p>{@code sharesHousehold} bypasses this block outright: the caller is declaring the owner a
 * genuine additional member of that household, so the create is allowed regardless.
 *
 * <p>Runs after {@link AssignHousehold} (so the new owner's {@code householdId}, and hence its
 * identity key, is set) and within the create transaction, but before {@link SaveOwner}, so the
 * new owner is not yet persisted and cannot collide with itself.
 */
public class RejectDuplicateIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member — bypass the duplicate block
        }
        String key = OwnerIdentity.key(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner does not block a new identity
            }
            if (key.equals(OwnerIdentity.key(existing))) {
                throw new DuplicateIdentityException(
                        "Another owner with the same identity already exists");
            }
        }
    }
}
