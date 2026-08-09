package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner that would silently join an existing household. Because the
 * {@code householdId} is now deterministic — computed from {@code (normalizedLastName, postcode)} by
 * {@link AssignHouseholdId} — two owners with the same last name and postcode are, by definition, the
 * same household. Creating a second such owner is therefore a household duplicate and is rejected with
 * 409 (Conflict) via {@link DuplicateHouseholdException}.
 *
 * <p>The request may acknowledge the shared household by setting {@code sharesHousehold} true; that
 * bypasses this block and the owner is created as a declared household member (and, being declared, is
 * not flagged as a possible duplicate by {@link AssignPossibleDuplicate}). Owners with no postcode have
 * no {@code householdId} and so are never household duplicates.
 *
 * <p>This is distinct from {@link EnsureOwnerIdentityUnique}, which rejects an exact identity match
 * (same telephone, email and household) regardless of {@code sharesHousehold}.
 *
 * <p>Runs after {@link AssignHouseholdId} (so the {@code householdId} is populated) and before
 * {@link SaveOwner}, so a duplicate is a 409 rather than a persisted row.
 */
public class EnsureHouseholdUnique {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository)
            throws DuplicateHouseholdException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // acknowledged: create as a declared household member
        }
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return; // no household to clash with
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is treated as absent
            }
            if (householdId.equals(existing.getHouseholdId())) {
                throw new DuplicateHouseholdException(householdId);
            }
        }
    }
}
