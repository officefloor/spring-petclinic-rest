package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-duplicate signal. The pipeline only reaches this step once
 * {@link CheckIdentityUnique} and {@link CheckHouseholdUnique} have passed, so the new owner is never
 * a <em>hard</em> duplicate here. A declared household member (one that set {@code sharesHousehold})
 * is never a suspected duplicate — it deliberately shares its household — so it is left unflagged.
 * Otherwise, when it shares an existing owner's last name and postcode while carrying a
 * <em>different</em> telephone, it is flagged as a possible duplicate: {@code possibleDuplicate} true
 * with {@code possibleDuplicateOf} set to the matching owner's id. Otherwise {@code possibleDuplicate}
 * is false and {@code possibleDuplicateOf} is left null.
 *
 * <p>Runs after {@link BuildOwner} and before {@link SaveOwner}, within the write transaction, so it
 * compares against the owners persisted so far (excluding this new, not-yet-saved one). The last name
 * is compared case-insensitively; a missing postcode can never match.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are ignored by the duplicate check
            }
            boolean sameLastName = lastName != null && lastName.equalsIgnoreCase(existing.getLastName());
            boolean samePostcode = postcode.equals(existing.getPostcode());
            boolean differentTelephone = telephone == null
                    ? existing.getTelephone() != null
                    : !telephone.equals(existing.getTelephone());
            if (sameLastName && samePostcode && differentTelephone) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
