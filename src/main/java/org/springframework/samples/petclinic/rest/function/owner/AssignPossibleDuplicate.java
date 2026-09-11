package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's {@code possibleDuplicate} / {@code possibleDuplicateOf}: a soft-match
 * flag for a new owner that is not a hard duplicate but still looks like one.
 *
 * <p>A create is only rejected (409) when the new owner exactly matches an existing owner's
 * {@link IdentityKeys identity key} (see {@link RequireUniqueIdentity}), so by the time this
 * step runs no existing owner shares that key. Here we flag the weaker signal: an existing
 * owner whose {@code identityKey} differs but whose {@code soundex(lastName)} and
 * {@code postcode} both match — i.e. the same household by name and postcode, distinguished
 * only by telephone or email. A <em>declared</em> household member ({@code sharesHousehold})
 * is intentionally never flagged — a declared member is not a suspected duplicate. Otherwise,
 * when a match is found the owner is still created, but with {@code possibleDuplicate} true
 * and {@code possibleDuplicateOf} set to the (lowest) matching owner id; when none is found
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left unset.
 *
 * <p>Runs after {@link BuildOwner} (so the owner's fields are set) and before
 * {@link SaveOwner} (so the new owner is not compared against itself).
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            // A declared household member is not a suspected duplicate.
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String soundex = Soundex.encode(lastName);
        String identityKey = IdentityKeys.forOwner(owner);
        Integer matchId = null;
        if (lastName != null && postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is not a possible duplicate
                }
                if (identityKey.equals(IdentityKeys.forOwner(existing))) {
                    continue; // an exact identity match is a hard duplicate, not a soft one
                }
                if (soundex.equals(Soundex.encode(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())
                        && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
