package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether the new owner is a suspected soft duplicate. A hard duplicate (same
 * {@link OwnerIdentity identity key}) is already rejected by {@link EnsureUniqueIdentity}, so any
 * owner reaching this step has a key of its own. This step marks a softer, sound-alike collision:
 * an existing owner whose last name has the same {@link Soundex} code and whose postcode matches,
 * yet whose identity key differs (a different person who might be the same). It sets
 * {@code possibleDuplicate} to whether such an owner exists, with {@code possibleDuplicateOf} set
 * to that owner's id.
 *
 * <p>A declared household member ({@code sharesHousehold}) knowingly joins the household, so it is
 * not a suspected duplicate and stays {@code possibleDuplicate = false}. A blank postcode has
 * nothing to match on and is likewise left unflagged.
 *
 * <p>Runs after {@link AssignIdentityKey} has set the identity key and before {@link SaveOwner}
 * persists the entity, so {@code findAll()} sees only the owners that existed before this create.
 * The flags are stored on the owner and returned on every later read.
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
        String soundex = Soundex.encode(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.isDeleted()) {
                continue; // a soft-deleted owner no longer collides
            }
            if (postcode.equals(existing.getPostcode())
                    && soundex.equals(Soundex.encode(existing.getLastName()))
                    && !identityKey.equals(existing.getIdentityKey())) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
