package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Records the id of an existing owner whose {@link IdentityKey} differs but whose {@link Soundex} of
 * last name and whose postcode both match — a soft, non-blocking duplicate. This catches household
 * members with different telephones (different key, same postcode and sound-alike name). A declared
 * household member ({@code sharesHousehold=true}) is not a suspected duplicate, so it is skipped, as is
 * an owner with no postcode. Runs before the owner is saved, so {@code findAll()} sees only
 * pre-existing owners. Leaves {@code possibleDuplicateOf} null when nothing matches.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isEmpty()) {
            return;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.forOwner(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (postcode.equals(existing.getPostcode())
                    && soundex.equals(Soundex.of(existing.getLastName()))
                    && !identityKey.equals(IdentityKey.forOwner(existing))) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
