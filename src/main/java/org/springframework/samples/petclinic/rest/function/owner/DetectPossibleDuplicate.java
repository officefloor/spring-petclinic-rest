package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match duplicate detection for create-owner. Runs after the household duplicate block
 * ({@link RejectDuplicateOwner}), so any owner reaching here that shares an existing owner's household
 * (same {@link HouseholdId} over last name and postcode) got here only by setting
 * {@code sharesHousehold} — it is a declared household member. A declared member is not a suspected
 * duplicate, so it is never flagged: {@code possibleDuplicate} is set false and
 * {@code possibleDuplicateOf} null.
 *
 * <p>Otherwise, if the owner nonetheless shares an existing owner's household id it is flagged:
 * {@code possibleDuplicate} true and {@code possibleDuplicateOf} the matching owner's id (the lowest id
 * when several match). Runs before the owner is saved, so {@link OwnerRepository#findAll()} sees only
 * owners that existed before this create.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // declared household member — not a suspected duplicate
        }
        String householdId = HouseholdId.of(owner.getLastName(), owner.getPostcode());
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            if (householdId.equals(HouseholdId.of(existing.getLastName(), existing.getPostcode()))) {
                if (match == null || existing.getId() < match.getId()) {
                    match = existing;
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
