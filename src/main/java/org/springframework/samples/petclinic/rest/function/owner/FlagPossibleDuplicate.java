package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that flags a soft duplicate. A new owner that is not a hard
 * duplicate (its {@link OwnerIdentity#key(String, String, String) identity key} differs from every
 * existing owner's — see {@link RequireUniqueIdentity}) but whose surname is phonetically the same
 * ({@link Soundex} of the last name) and whose postcode matches an existing owner is a
 * <em>possible</em> duplicate: it is still created, but {@code possibleDuplicate} is set {@code true}
 * and {@code possibleDuplicateOf} to the matching owner's id. Otherwise {@code possibleDuplicate} is
 * {@code false} and {@code possibleDuplicateOf} is left unset.
 *
 * <p>A declared household member (the request set {@code sharesHousehold} to bypass the duplicate
 * block) is <em>not</em> a suspected duplicate: it shares an existing owner's surname and postcode by
 * declaration, so it is never flagged, whatever its telephone.
 *
 * <p>The match is on soundex(lastName) and postcode; an existing owner whose identity key equals the
 * new owner's would already have been rejected as a hard duplicate, so it is skipped here defensively.
 * When several existing owners match, the one with the lowest id (the earliest) is reported. Runs
 * after {@link BuildOwner} so the owner's telephone is already normalized, and before
 * {@link SaveOwner} so the owner being created is not compared against itself.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false); // a declared household member is not a suspected duplicate
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String postcode = owner.getPostcode();
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue; // a soft-deleted owner is not a possible-duplicate match
                }
                if (existing.getId() == null || !postcode.equals(existing.getPostcode())
                        || !soundex.equals(Soundex.of(existing.getLastName()))) {
                    continue;
                }
                if (identityKey.equals(OwnerIdentity.key(existing.getTelephone(), existing.getEmail(),
                        existing.getLastName()))) {
                    continue; // same identity key: a hard duplicate, not a possible one
                }
                if (matchId == null || existing.getId() < matchId) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
