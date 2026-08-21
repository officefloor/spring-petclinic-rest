package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Stamps the soft-duplicate flags onto the newly built owner. A create that is not a hard
 * duplicate (already enforced by {@link CheckOwnerIdentityUnique}) is still flagged as a
 * possible duplicate when it shares an existing owner's postcode and phonetic last name
 * ({@link Soundex} code) while carrying a <em>different</em> {@link OwnerIdentityKey identity key}
 * — typically a different telephone or email. When such a match is found, {@code possibleDuplicate}
 * is set {@code true} and {@code possibleDuplicateOf} to the matching owner's id (the
 * earliest-created match when several exist); otherwise {@code possibleDuplicate} is {@code false}
 * and {@code possibleDuplicateOf} is left absent.
 *
 * <p>A declared household member (the request opted in with {@code sharesHousehold} true) is never
 * flagged: a member the caller has explicitly declared is not a <em>suspected</em> duplicate.
 *
 * <p>Runs before {@link SaveOwner}, so the new owner is not yet persisted and cannot match itself.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request,
            OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // no postcode to share — cannot be a possible duplicate
        }
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = OwnerIdentityKey.forOwner(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (postcode.equals(existing.getPostcode())
                    && soundex.equals(Soundex.encode(existing.getLastName()))
                    && !identityKey.equals(OwnerIdentityKey.forOwner(existing))) {
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
