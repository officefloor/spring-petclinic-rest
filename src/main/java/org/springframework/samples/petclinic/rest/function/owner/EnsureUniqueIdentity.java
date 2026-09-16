package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DuplicateIdentityException;

/**
 * The duplicate block: rejects a create that re-registers the same person as an existing
 * owner, as a 409 via {@link DuplicateIdentityException}. A person is identified by their
 * {@code (lastName, postcode)} household — its deterministic {@code householdId} (see
 * {@link Households#householdId}) — together with their telephone; a request matching an
 * existing owner on both is the same person registering twice.
 *
 * <p>A second owner in the same household with a <em>different</em> telephone is a distinct
 * person and is allowed through, to be created and flagged as a soft duplicate (see
 * {@link AssignPossibleDuplicate}); this is how a household comes to have more than one
 * member. A request that opts in with {@code sharesHousehold=true} is a declared household
 * member and bypasses the block (and is left unflagged). A request with no postcode has no
 * household key and cannot collide.
 *
 * <p>Runs after the telephone is normalized to E.164 (matching the stored form) and before
 * {@link BuildOwner}, so a collision is caught before any owner is built or saved.
 */
public class EnsureUniqueIdentity {

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DuplicateIdentityException {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String householdId = Households.householdId(request.getLastName(), request.getPostcode());
        if (householdId == null) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner no longer occupies its household
            }
            if (householdId.equals(existing.getHouseholdId())
                    && Objects.equals(request.getTelephone(), existing.getTelephone())) {
                throw new DuplicateIdentityException(householdId);
            }
        }
    }
}
