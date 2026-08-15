package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns the owner's soft-match flags {@code possibleDuplicate} / {@code possibleDuplicateOf}. An
 * exact-identity duplicate has already been rejected with 409 by {@link CheckOwnerIdentityUnique}, so
 * every owner reaching this step is being created. This step flags the weaker overlap: an owner that
 * shares an existing owner's {@link Soundex Soundex code} of the last name and its {@code postcode}
 * but carries a <em>different</em> {@code identityKey} (see {@link OwnerIdentityKey}). Because the
 * telephone (and email) are part of that key, two owners with the same last name and postcode but a
 * different telephone differ in their key and so are flagged here rather than rejected. When such a
 * match exists {@code possibleDuplicate} is set true and {@code possibleDuplicateOf} to the matching
 * owner's id (the lowest id when several match, for determinism); otherwise {@code possibleDuplicate}
 * is false and {@code possibleDuplicateOf} left absent.
 *
 * <p>A declared household member (a request with {@code sharesHousehold} true) is <em>not</em> a
 * suspected duplicate: it has explicitly declared it shares the household, so its flags are left
 * unset.
 *
 * <p>Runs after {@link BuildOwner} has produced the {@link Owner} and before {@link SaveOwner}
 * persists it, so {@link OwnerRepository#findAll()} returns only the pre-existing owners and the new
 * owner never matches itself. Mutates the entity in place so {@link SaveOwner} stores the values and
 * later reads return them.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Household.sharesHousehold(request)) {
            // A declared household member is not a suspected duplicate.
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String postcode = owner.getPostcode();
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            String soundex = Soundex.soundex(owner.getLastName());
            String identityKey = OwnerIdentityKey.forOwner(owner);
            for (Owner other : ownerRepository.findAll()) {
                if (other.isDeleted()) {
                    continue; // a soft-deleted owner is not a possible-duplicate match
                }
                if (soundex.equals(Soundex.soundex(other.getLastName()))
                        && postcode.equals(other.getPostcode())
                        && !identityKey.equals(OwnerIdentityKey.forOwner(other))) {
                    if (matchId == null || (other.getId() != null && other.getId() < matchId)) {
                        matchId = other.getId();
                    }
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }
}
