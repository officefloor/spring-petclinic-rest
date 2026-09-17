package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Flags a soft duplicate: a new owner that passed the duplicate block (see
 * {@link EnsureUniqueIdentity}) with a <em>different</em> {@link IdentityKey identity key} from every
 * existing owner, yet still shares one's like-sounding last name ({@link Soundex}) and postcode. This
 * is the household-member case — same name and postcode but a different telephone, so a different
 * key. Such a request is allowed but marked for follow-up: the built {@link Owner} records
 * {@code possibleDuplicate} true and {@code possibleDuplicateOf} the matching owner's id (the earliest
 * match when several exist). A <em>declared</em> household member (the request set
 * {@code sharesHousehold}) is never flagged — deliberately sharing a household is not a suspected
 * duplicate. When nothing matches, {@code possibleDuplicate} is false and {@code possibleDuplicateOf}
 * stays absent. Runs before {@link SaveOwner}, so the new owner is not yet persisted and is never
 * matched against itself. Soft-deleted owners are ignored, mirroring the duplicate block in
 * {@link EnsureUniqueIdentity}. Mutates the built {@link Owner} in place.
 */
public class FlagPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String identityKey = IdentityKey.forOwner(owner);
        String soundex = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isActive()
                    && !identityKey.equals(IdentityKey.forOwner(existing))
                    && soundex.equals(Soundex.of(existing.getLastName()))
                    && Objects.equals(postcode, existing.getPostcode())) {
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
