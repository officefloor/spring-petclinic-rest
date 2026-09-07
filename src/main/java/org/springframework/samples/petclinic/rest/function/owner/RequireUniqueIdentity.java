package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * Step of {@code POST /api/owners}, the duplicate block, that rejects a create whose derived identity
 * (see {@link OwnerIdentity}) collides with an existing owner's, throwing
 * {@link DuplicateIdentityException} (handled as 409 Conflict).
 *
 * <p>Two owners are the same <em>household</em> when they share a computed {@link Household#id(String,
 * String) householdId} (same last name and postcode). A second owner in an existing household is a
 * household duplicate and is rejected — <em>unless</em> the request sets {@code sharesHousehold},
 * which bypasses this block so the owner is created as a declared household member. The
 * {@code householdId} is derived from the fields, not linked at create time, so members of a
 * household always share it automatically.
 *
 * <p>Independently, two owners that are the same contact (matching telephone and email — the
 * {@link OwnerIdentity#contactKey(String, String) contactKey}) collide regardless of household;
 * {@code sharesHousehold} does not bypass that, since a declared member is a distinct contact.
 *
 * <p>Runs after {@link RequireOwnerFields} has normalized and published the body and before
 * {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        boolean sharesHousehold = Boolean.TRUE.equals(request.getSharesHousehold());
        String householdId = Household.id(request.getLastName(), request.getPostcode());
        String contactKey = OwnerIdentity.contactKey(request.getTelephone(), request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            String existingKey = OwnerIdentity.contactKey(existing.getTelephone(), existing.getEmail());
            if (contactKey.equals(existingKey)) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity already exists");
            }
            if (!sharesHousehold
                    && householdId.equals(Household.id(existing.getLastName(), existing.getPostcode()))) {
                throw new DuplicateIdentityException(
                        "An owner in the same household already exists");
            }
        }
    }
}
