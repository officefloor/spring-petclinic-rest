package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records a soft-duplicate match before {@link SaveOwner} runs. The create has already
 * passed {@link CheckUniqueIdentity}, so it is not a hard duplicate. When it still shares
 * an existing owner's lastName (compared case-insensitively with collapsed whitespace)
 * and postcode but carries a different telephone, the matched owner's id is stored on
 * {@code possibleDuplicateOf}, which surfaces as {@code possibleDuplicate}/{@code
 * possibleDuplicateOf} on the response. Left unset (so {@code possibleDuplicate} is false)
 * otherwise.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String lastName = CheckUniqueHousehold.normalize(owner.getLastName());
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && postcode.equals(other.getPostcode())
                    && lastName.equals(CheckUniqueHousehold.normalize(other.getLastName()))
                    && !postcode.isBlank()
                    && !equalsTelephone(owner, other)) {
                owner.setPossibleDuplicateOf(other.getId());
                return;
            }
        }
    }

    private static boolean equalsTelephone(Owner owner, Owner other) {
        String telephone = owner.getTelephone();
        return telephone != null && telephone.equals(other.getTelephone());
    }
}
