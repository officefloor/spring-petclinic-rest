package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that flags a soft ("possible") duplicate. The hard-duplicate
 * check ({@link EnsureUniqueIdentity}) has already rejected an exact {@code identityKey} collision
 * with 409, so this owner is being created. This step then marks it as a possible duplicate when its
 * identityKey <em>differs</em> from an existing owner's yet its {@link Soundex} last name and postcode
 * both match — the same household by phonetic surname and postcode, reached by a different telephone
 * or email.
 *
 * <p>When the request opts into the household via {@code sharesHousehold} the owner is a
 * <em>declared</em> household member, not a suspected one, so it is never flagged. Otherwise, when a
 * matching existing owner is found the new owner's {@code possibleDuplicate} is set true and
 * {@code possibleDuplicateOf} to that owner's id (the lowest matching id when several match);
 * otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is null. A missing
 * postcode never matches, since the rule keys on soundex(lastName) <em>and</em> postcode. Runs after
 * {@link EnsureUniqueIdentity} and before {@link SaveOwner}, so the flags are persisted with the owner.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return; // a declared household member is not a suspected duplicate
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.of(owner);
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // the owner being created is not its own possible duplicate
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a possible-duplicate match
            }
            if (IdentityKey.of(existing).equals(identityKey)) {
                continue; // an identical identityKey is a hard-duplicate concern, not a soft match
            }
            if (!Soundex.of(existing.getLastName()).equals(soundex)) {
                continue;
            }
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (match == null || existing.getId() < match.getId()) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }
}
