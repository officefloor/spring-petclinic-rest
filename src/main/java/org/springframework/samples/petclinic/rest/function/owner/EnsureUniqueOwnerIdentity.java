package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateOwnerException;

/**
 * The single duplicate check on create, keyed on the owner's identityKey — the SHA-256 hex over
 * the {@code normalizedTelephone|email|soundex(lastName)} composite defined by
 * {@link OwnerIdentity}. Only an existing owner with the same identityKey is a hard duplicate,
 * rejected 409 via {@link DuplicateOwnerException}. Because the telephone is part of the key,
 * two owners with the same lastName and postcode but a different telephone are <em>not</em> a
 * hard duplicate: they may coexist as household members — which is what makes the household
 * membership-level ceiling meaningful — and are instead soft-flagged downstream by
 * {@link AssignPossibleDuplicate}. There is no separate household-duplicate block; the identityKey
 * is the whole duplicate rule.
 *
 * <p>{@code sharesHousehold} bypasses this block entirely: a declared household member is always
 * allowed (and is not flagged as a possible duplicate downstream). Runs before the owner is saved.
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
