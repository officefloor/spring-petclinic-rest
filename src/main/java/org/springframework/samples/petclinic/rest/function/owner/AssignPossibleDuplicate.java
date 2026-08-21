package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-match detection for the create-owner pipeline. By the time this step runs the owner is
 * already known not to be a hard duplicate (an exact {@code identityKey} match is rejected earlier
 * with 409 by {@link CheckOwnerIdentityUnique}), so this step never sees one. It instead flags a
 * weaker resemblance: an owner that shares an existing owner's {@code lastName} and {@code postcode}
 * but has a different {@code telephone}. Such an owner is still created, with
 * {@code possibleDuplicate} set true and {@code possibleDuplicateOf} set to the matching owner's id;
 * otherwise the flag is false and the id null.
 *
 * <p>A <em>declared</em> household member (the request set {@code sharesHousehold}) is never
 * flagged: sharing a last name and postcode is exactly what a declared member does, so it is an
 * announced household, not a suspected duplicate. The flag is left false for such owners.
 *
 * <p>Runs after the owner's fields are finalized and before {@link SaveOwner}, so the comparison is
 * against every already-persisted owner and the new owner (not yet saved) is excluded. When several
 * existing owners match, the one with the smallest id wins, so the result is deterministic.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (lastName == null || postcode == null || telephone == null) {
            return;
        }

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equalsIgnoreCase(existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !telephone.equals(existing.getTelephone())
                    && existing.getId() != null
                    && (matchId == null || existing.getId() < matchId)) {
                matchId = existing.getId();
            }
        }

        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }
}
