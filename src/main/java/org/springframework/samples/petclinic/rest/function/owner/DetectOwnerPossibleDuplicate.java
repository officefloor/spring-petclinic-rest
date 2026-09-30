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
 * joining an existing household is allowed (its membership level is capped by
 * {@link CapOwnerMembershipLevel}) but flagged here as a possible duplicate, keying the soft signal
 * off the same {@link HouseholdMembers household membership} as the size and cap steps.
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
            for (Owner member : HouseholdMembers.of(owner, ownerRepository)) {
                if (member.getId() == null) {
                    continue; // an unsaved member has no id to record as possibleDuplicateOf
                }
                if (match == null || member.getId() < match.getId()) {
                    match = member;
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
