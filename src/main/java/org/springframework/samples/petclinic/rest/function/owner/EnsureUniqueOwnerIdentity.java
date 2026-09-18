package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Runs in {@code POST /api/owners} after {@link ValidateOwnerFields}, reading the
 * already-normalized body as a variable. Enforces one household per (lastName, postcode): it
 * derives the request's {@link Household#id(String, String) household id} and rejects the request
 * with a 409 when an existing owner already belongs to that household, before any entity is built
 * or persisted.
 *
 * <p>A request that opts in with {@code sharesHousehold=true} bypasses this block: it is a
 * declared member of the household and is created alongside the existing one(s). Reads the same
 * transaction as the writes.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerIdentityException {

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member bypasses the duplicate block
        }

        String householdId = Household.id(request.getLastName(), request.getPostcode());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer holds its household identity
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateOwnerIdentityException(householdId);
            }
        }
    }
}
