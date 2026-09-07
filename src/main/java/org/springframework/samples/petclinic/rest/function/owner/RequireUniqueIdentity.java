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
 * <p>Two owners that are the same contact (matching telephone and email — the
 * {@link OwnerIdentity#contactKey(String, String) contactKey}) collide and are rejected. A second,
 * distinct contact that shares a household ({@link Household#id(String, String) same householdId} —
 * same last name and postcode) is <em>not</em> a duplicate: it is a permitted household member, whose
 * {@code membershipLevel} is instead bounded by {@link CapMembershipLevel}.
 *
 * <p>Runs after {@link RequireOwnerFields} has normalized and published the body and before
 * {@link BuildOwner}/{@link SaveOwner} persist the new owner.
 */
public class RequireUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        String contactKey = OwnerIdentity.contactKey(request.getTelephone(), request.getEmail());
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer blocks a create
            }
            String existingKey = OwnerIdentity.contactKey(existing.getTelephone(), existing.getEmail());
            if (contactKey.equals(existingKey)) {
                throw new DuplicateIdentityException(
                        "An owner with the same identity already exists");
            }
        }
    }
}
