package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Flags the freshly built {@link Owner} as a soft (non-blocking) duplicate before it is saved.
 * A soft duplicate is one that is <em>not</em> a hard duplicate (an exact {@code identityKey}
 * match, already rejected with 409 by {@link CheckUniqueIdentity}) but whose last name has the same
 * {@code soundex} code and whose {@code postcode} matches an existing owner. Because the telephone
 * (and email) are part of the {@code identityKey}, two owners with the same last name and postcode
 * but a different telephone have differing keys and so fall through to here as a soft match.
 *
 * <p>When such an existing owner is found the new owner is still created, with
 * {@code possibleDuplicate} set to {@code true} and {@code possibleDuplicateOf} set to the matching
 * owner's id (the earliest such owner when several match); otherwise {@code possibleDuplicate} is
 * {@code false} and {@code possibleDuplicateOf} is left unset.
 *
 * <p>Runs after {@link CheckUniqueIdentity} (so hard duplicates are already out) and before
 * {@link SaveOwner} (so the new owner is not compared against itself).
 *
 * <p>An owner that declared {@code sharesHousehold=true} is an intentional household member, not a
 * suspected duplicate, so it is never flagged: {@code possibleDuplicate} is left {@code false}.
 */
public class CheckPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate.
        }
        String identityKey = OwnerIdentities.identityKey(owner);
        String soundex = OwnerIdentities.soundex(owner.getLastName());
        String postcode = normalize(owner.getPostcode());

        Integer matchId = null;
        if (!soundex.isEmpty() && !postcode.isEmpty()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                    continue; // never match self
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is not a suspected duplicate match.
                }
                if (!identityKey.equals(OwnerIdentities.identityKey(existing))
                        && soundex.equals(OwnerIdentities.soundex(existing.getLastName()))
                        && postcode.equals(normalize(existing.getPostcode()))) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
