package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Flags a soft duplicate: an owner that passed the hard-duplicate check (see
 * {@link RequireUniqueIdentity}) but still looks like it may duplicate an existing one because it
 * shares that owner's postcode and phonetically-equal last name ({@link Soundex}) while resolving
 * to a different {@code identityKey} (see {@link OwnerIdentities}) — i.e. a different telephone or
 * email. Sets {@link Owner#getPossibleDuplicate() possibleDuplicate} true and
 * {@link Owner#getPossibleDuplicateOf() possibleDuplicateOf} to the matching owner's id; otherwise
 * leaves the flag false and no reference.
 *
 * <p>A declared household member — one that opted in with {@code sharesHousehold} to join an
 * existing (lastName, postcode) household — is never flagged: a declared member is not a
 * suspected duplicate.
 *
 * <p>Runs before {@link SaveOwner} so it compares only against already-stored owners and never
 * itself. When several owners match, the lowest id is chosen for a deterministic reference.
 */
public class FlagOwnerPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        Integer matchId = null;
        boolean declaredMember = Boolean.TRUE.equals(request.getSharesHousehold());
        if (!declaredMember && owner.getPostcode() != null) {
            String identityKey = OwnerIdentities.of(owner);
            String soundex = Soundex.encode(owner.getLastName());
            for (Owner existing : ownerRepository.findAll()) {
                if (owner.getPostcode().equals(existing.getPostcode())
                        && soundex.equals(Soundex.encode(existing.getLastName()))
                        && !identityKey.equals(OwnerIdentities.of(existing))
                        && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
