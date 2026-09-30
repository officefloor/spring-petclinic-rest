package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check on create, keyed on the owner's full identity — the
 * {@code normalizedTelephone|email|householdId} composite defined by {@link OwnerIdentity}.
 * Only an existing owner with the same identity is a hard duplicate, rejected 409 via
 * {@link DuplicateOwnerException}. Two owners sharing a household (same lastName and postcode)
 * but differing in telephone or email are therefore <em>not</em> a hard duplicate: they may
 * coexist as household members — which is what makes the household membership-level ceiling
 * meaningful — and are instead soft-flagged downstream by {@link AssignPossibleDuplicate}.
 *
 * <p>{@code sharesHousehold} bypasses this block entirely: a declared household member is always
 * allowed (and is not flagged as a possible duplicate downstream). Runs after the householdId is
 * assigned and before the owner is saved.
 */
public class EnsureUniqueOwnerIdentity {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateOwnerException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member bypasses the duplicate block
        }
        String identity = OwnerIdentity.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners do not block a create
            }
            if (identity.equals(OwnerIdentity.of(existing))) {
                throw new DuplicateOwnerException(identity);
            }
        }
    }
}
