package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Soundex;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match step in the create-owner pipeline. Runs after {@link BuildOwner} (so the owner carries
 * its normalized telephone, lastName and postcode) and before {@link SaveOwner}. It is reached only
 * for owners that already passed {@link EnsureUniqueIdentity}, i.e. that are NOT hard duplicates.
 *
 * <p>An owner is a <em>possible</em> duplicate when an existing owner shares its
 * {@code soundex(lastName)} and {@code postcode} but has a different {@code identityKey}. When such
 * an owner exists the new owner is still created, but flagged with {@code possibleDuplicate = true}
 * and {@code possibleDuplicateOf} set to the matching owner's id (the lowest id when several match).
 * Otherwise {@code possibleDuplicate} is set to {@code false} and no match id is recorded.
 *
 * <p>Because the telephone is part of the identityKey, two owners with the same lastName and
 * postcode but different telephones have different keys — they are no longer a hard household
 * duplicate but exactly this soft match.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        if (lastName == null || postcode == null || postcode.isBlank()) {
            return; // a shared soundex(lastName)+postcode is required to be a possible duplicate
        }
        String soundex = Soundex.encode(lastName);
        String identityKey = owner.getIdentityKey();

        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() == null) {
                continue;
            }
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // soft-deleted owners are not counted as possible duplicates
            }
            if (soundex.equals(Soundex.encode(existing.getLastName()))
                    && postcode.equals(existing.getPostcode())
                    && !identityKey.equals(existing.getIdentityKey())) {
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }

        if (matchId != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(matchId);
        }
    }
}
