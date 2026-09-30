package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-duplicate flags. Runs after {@link EnsureUniqueOwnerIdentity}
 * has already rejected exact identityKey duplicates, so any match here is a genuinely different
 * owner. A soft (possible) duplicate is an existing owner whose {@code identityKey} differs from
 * this owner's but whose surname shares the same {@link Soundex} code and whose {@code postcode}
 * matches — for example the same lastName and postcode with a different telephone, which the
 * identity key (telephone-inclusive) no longer treats as a hard duplicate. When one exists the
 * owner is still created, with {@code possibleDuplicate} true and {@code possibleDuplicateOf} set
 * to the matching owner's id; otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is null. The earliest (smallest id) match is chosen so the result
 * is deterministic.
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

        String identity = OwnerIdentity.of(owner);
        String surname = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();

        Owner match = null;
        if (owner.getLastName() != null && postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue;
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // soft-deleted owners are not flagged as possible duplicates
                }
                if (!identity.equals(OwnerIdentity.of(existing))
                        && surname.equals(Soundex.of(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())) {
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
