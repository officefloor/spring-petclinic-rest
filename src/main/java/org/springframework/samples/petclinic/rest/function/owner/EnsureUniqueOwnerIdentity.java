package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerIdentityException;

/**
 * Runs in {@code POST /api/owners} after {@link ValidateOwnerFields}, reading the
 * already-normalized body as a variable. Rejects a <em>hard</em> duplicate — an existing owner with
 * the same identity, i.e. the same {@link Household#id(String, String) household id} (lastName +
 * postcode) <em>and</em> the same telephone — with a 409, before any entity is built or persisted.
 *
 * <p>A same-household owner with a <em>different</em> telephone is not a hard duplicate: it is
 * created and flagged downstream as a possible duplicate by {@link AssignOwnerPossibleDuplicate}.
 * A request that opts in with {@code sharesHousehold=true} bypasses this block entirely: it is a
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
            if (householdId.equals(existing.getHouseholdId())
                    && request.getTelephone().equals(existing.getTelephone())) {
                throw new DuplicateOwnerIdentityException(householdId);
            }
        }
    }
}
