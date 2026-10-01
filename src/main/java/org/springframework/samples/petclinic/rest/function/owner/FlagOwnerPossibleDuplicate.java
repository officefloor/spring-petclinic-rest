package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the owner as a <em>possible</em> (soft) duplicate of an existing owner.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link FlagOwnerBulkSignup} and before
 * {@link SaveOwner}, so the new owner is not yet persisted and therefore not compared against
 * itself. A soft match is a weaker signal than the hard-duplicate {@link CheckOwnerDuplicateIdentity
 * identity-key} rule: an existing owner whose {@link OwnerIdentity identity key} <em>differs</em>
 * (so it is not a hard duplicate) yet whose last name sounds alike ({@link OwnerSoundex#soundex
 * same Soundex code}) and whose postcode is equal. People who really are distinct but look alike —
 * phonetically similar family name, same postcode, different phone or email — are still created, but
 * flagged for review.
 *
 * <p>When such a match is found, {@code possibleDuplicate} is set to {@code true} and
 * {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several match, so the
 * result is deterministic); otherwise {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} stays null. A postcode is required on both sides for a match — an
 * owner with no postcode never soft-matches. The values are frozen at creation time and returned
 * unchanged on later reads.
 *
 * <p>A <em>declared</em> household member — a request that opted in with {@code sharesHousehold: true}
 * to join an existing household (same last name and postcode) — is never flagged: a declared member
 * is not a suspected duplicate. Such a request is left with {@code possibleDuplicate = false}.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String postcode = owner.getPostcode();
        String soundex = OwnerSoundex.soundex(owner.getLastName());
        String identityKey = OwnerIdentity.identityKey(owner);

        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue;
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // soft-deleted owners are ignored by the duplicate check
                }
                if (identityKey.equals(OwnerIdentity.identityKey(existing))) {
                    continue; // a matching identity key is the hard-duplicate case, not a soft match
                }
                if (soundex.equals(OwnerSoundex.soundex(existing.getLastName()))
                        && postcode.equals(existing.getPostcode())) {
                    if (matchId == null || existing.getId() < matchId) {
                        matchId = existing.getId();
                    }
                }
            }
        }

        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
