package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Rejects a create-owner request that would land in a household that already exists. The household
 * is keyed on {@code (lastName, postcode)} through the deterministic {@code householdId} assigned by
 * {@link AssignHousehold}, so a second owner sharing an existing owner's last name and postcode is a
 * household duplicate and is rejected with 409 (see {@link DuplicateIdentityException}).
 *
 * <p>A request may opt in with {@code sharesHousehold=true} to declare that it intentionally joins
 * that household. Doing so <em>bypasses this block</em> — the owner is created as a declared
 * household member — so {@code sharesHousehold} now only lifts the duplicate rejection rather than
 * creating the link (the link is implicit in the shared {@code householdId}).
 *
 * <p>Runs after {@link BuildOwner} and {@link AssignHousehold} so the new owner's {@code householdId}
 * is already assigned, and before {@link SaveOwner} so the new owner is not compared against itself.
 */
public class CheckUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member: bypass the duplicate block.
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never collide with self (should not yet be persisted, but be safe)
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a new registration.
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
