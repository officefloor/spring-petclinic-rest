package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerTelephones;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-duplicate flags. Runs after {@link EnsureUniqueOwnerIdentity}
 * has already rejected exact identity duplicates, so any match here is a genuinely different
 * owner. A soft (possible) duplicate is an existing owner that shares this owner's
 * {@code lastName} (case-insensitive) and {@code postcode} but has a different (normalized)
 * telephone. When one exists the owner is still created, with {@code possibleDuplicate} true
 * and {@code possibleDuplicateOf} set to the matching owner's id; otherwise
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is null. The earliest
 * (smallest id) match is chosen so the result is deterministic.
 *
 * <p>A declared household member ({@code sharesHousehold} true) is never flagged: it was
 * created deliberately as part of the household, so it is not a suspected duplicate.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = OwnerTelephones.toE164OrRaw(owner.getTelephone());

        Owner match = null;
        if (lastName != null && postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue;
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // soft-deleted owners are not flagged as possible duplicates
                }
                if (lastName.equalsIgnoreCase(existing.getLastName())
                        && postcode.equals(existing.getPostcode())
                        && !telephone.equals(OwnerTelephones.toE164OrRaw(existing.getTelephone()))) {
                    if (match == null || existing.getId() < match.getId()) {
                        match = existing;
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
