package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a soft (possible) duplicate. An owner whose WHOLE {@link OwnerIdentity#identityKey
 * identityKey} differs from every existing owner's (so it is not a hard duplicate — that check
 * runs earlier and rejects with 409) but that shares an existing owner's last-name
 * {@link Soundex} code and {@code postcode} is still created, with {@code possibleDuplicate} set
 * true and {@code possibleDuplicateOf} set to the matching owner's id. When several owners match,
 * the earliest (lowest id) is chosen. Otherwise {@code possibleDuplicate} is false and
 * {@code possibleDuplicateOf} is absent.
 *
 * <p>A declared household member — the request set {@code sharesHousehold} true — is not a
 * suspected duplicate, so it is never flagged.
 *
 * <p>Runs before {@link SaveOwner}, so the new owner is not yet persisted and only pre-existing
 * owners are considered.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner,
            OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return; // a declared household member is not a suspected duplicate
        }
        String soundex = Soundex.of(owner.getLastName());
        String postcode = owner.getPostcode();
        String key = OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(),
                owner.getLastName());
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank() && !soundex.isEmpty()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (existing.getId() == null || existing.getId().equals(owner.getId())) {
                    continue; // never match the new owner against itself
                }
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is not a possible duplicate
                }
                String other = OwnerIdentity.identityKey(existing.getTelephone(),
                        existing.getEmail(), existing.getLastName());
                if (!key.equals(other)
                        && soundex.equals(Soundex.of(existing.getLastName()))
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
