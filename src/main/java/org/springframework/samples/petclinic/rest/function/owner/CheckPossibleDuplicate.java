package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-duplicate flagging step of {@code POST /api/owners}. Runs after {@link CheckOwnerIdentityUnique}
 * (so the new owner is already known not to be a hard, {@code identityKey} duplicate) and before
 * {@link SaveOwner}.
 *
 * <p>A new owner is a <em>possible</em> duplicate when, though not a hard duplicate, it belongs to the
 * same household as an existing owner — same {@code householdId}, the single {@link OwnerHousehold}
 * definition keyed on {@code lastName} and {@code postcode} — while having a different {@code telephone}.
 * Such an owner is still created, but flagged: {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to the matching existing owner's id (the lowest id when more than one
 * matches). When there is no match the owner is flagged {@code possibleDuplicate} false. The new owner
 * is still unsaved, so it is not among the owners iterated here and cannot match itself.
 *
 * <p>A <em>declared</em> household member — one whose request set {@code sharesHousehold} — is
 * deliberately exempt: it is a known, acknowledged second member of the household, not a suspected
 * duplicate, so it is never flagged.
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // declared household member is not a suspected duplicate
        }

        String householdId = owner.getHouseholdId();
        String telephone = owner.getTelephone();
        if (householdId == null) {
            return; // no postcode, so no household to match on
        }

        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())
                    && !OwnerMatch.equalsIgnoreCase(telephone, existing.getTelephone())) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
