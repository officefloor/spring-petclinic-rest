package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners}: records whether the new owner is a possible duplicate of
 * an existing one — sharing the same last name and postcode but a different telephone (see
 * {@link PossibleDuplicate}). Such an owner is still created; the matching owner's id is
 * stored in {@code possibleDuplicateOf} so a later read can flag it.
 *
 * <p>Runs after {@link EnsureUniqueIdentity} has already rejected exact-identity duplicates, so
 * a match reaching here is a distinct additional household member — either a declared one
 * ({@code sharesHousehold}) or one admitted by its different telephone. A declared member is
 * not a suspected duplicate, so it is never flagged; otherwise the earliest-created (lowest id)
 * matching owner is recorded, and no match leaves the field unset.
 */
public class FlagPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicateOf(null); // a declared household member is not a suspected duplicate
            return;
        }
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner is not a duplicate to flag against
            }
            if (PossibleDuplicate.matches(existing, owner)) {
                Integer id = existing.getId();
                if (id != null && (matchId == null || id < matchId)) {
                    matchId = id;
                }
            }
        }
        owner.setPossibleDuplicateOf(matchId);
    }
}
