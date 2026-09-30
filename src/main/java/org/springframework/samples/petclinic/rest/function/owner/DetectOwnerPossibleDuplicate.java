package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags the new owner as a possible (soft) duplicate, keyed off the computed
 * {@link Owner#getHouseholdId() householdId}: an owner is a possible duplicate when an earlier owner
 * shares its household (same lastName and postcode).
 *
 * <p>A <em>declared</em> household member — one created with {@code sharesHousehold} true — is never
 * flagged: it deliberately joins the household, so it is not a suspected duplicate. Any other owner
 * sharing an existing household has already been rejected 409 by {@link CheckOwnerHouseholdUnique},
 * so in practice this leaves {@code possibleDuplicate} false; keeping the check here keys the soft
 * signal off the same household id as the hard block.
 *
 * <p>Runs after {@link BuildOwner}/{@link AssignOwnerHousehold} and before {@link SaveOwner},
 * scanning only pre-existing (already persisted) owners — the new owner has no id yet and is skipped.
 * The earliest such match (lowest id) is recorded: {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to that owner's id; otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is left null. Both are stored on the built owner so the saved and
 * returned record carries them.
 */
public class DetectOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        Owner match = null;
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            String householdId = owner.getHouseholdId();
            if (householdId != null) {
                for (Owner existing : ownerRepository.findAll()) {
                    if (existing.getId() == null
                            || existing.getId().equals(owner.getId())) {
                        continue;
                    }
                    if (existing.isDeleted()) {
                        continue; // soft-deleted owners are ignored by the duplicate check
                    }
                    if (householdId.equals(existing.getHouseholdId())) {
                        if (match == null || existing.getId() < match.getId()) {
                            match = existing;
                        }
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
        else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
