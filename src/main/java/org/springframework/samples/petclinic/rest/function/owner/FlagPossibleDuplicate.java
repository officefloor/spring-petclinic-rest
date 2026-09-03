package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records a soft-duplicate match before {@link SaveOwner} runs. The create has already
 * passed {@link CheckUniqueIdentity}, so it is not a hard duplicate. When its identityKey
 * still differs from an existing owner's yet its lastName is phonetically equal (same
 * {@link Soundex} code) and its postcode matches, the matched owner's id is stored on
 * {@code possibleDuplicateOf}, which surfaces as {@code possibleDuplicate}/{@code
 * possibleDuplicateOf} on the response. Left unset (so {@code possibleDuplicate} is false)
 * otherwise, and always for a declared household member ({@code sharesHousehold=true}),
 * which is not a suspected duplicate.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String soundex = Soundex.code(owner.getLastName());
        String key = CheckUniqueIdentity.identityKey(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && postcode.equals(other.getPostcode())
                    && soundex.equals(Soundex.code(other.getLastName()))
                    && !postcode.isBlank()
                    && !key.equals(CheckUniqueIdentity.identityKey(other))) {
                owner.setPossibleDuplicateOf(other.getId());
                return;
            }
        }
    }
}
