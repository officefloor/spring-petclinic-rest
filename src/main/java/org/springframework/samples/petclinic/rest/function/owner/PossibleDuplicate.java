package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Finds the existing owner this owner is a possible (soft) duplicate of: another owner sharing this
 * owner's {@link Soundex} last name and postcode but with a different {@link IdentityKey}. A shared
 * identity key would be a hard duplicate (rejected at create, see {@link RejectDuplicateOwnerIdentity}),
 * so a differing key on a like-sounding name at the same postcode is exactly the soft-match case.
 * Owners without a postcode never soft-match. Returns the matching owner's id (the lowest when
 * several match), or {@code null} when there is no possible duplicate.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    public static Integer of(Owner owner, OwnerRepository repository) {
        if (Boolean.TRUE.equals(owner.getSharesHousehold())) {
            return null;
        }
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.of(owner);
        Integer match = null;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId()) && postcode.equals(other.getPostcode())
                    && soundex.equals(Soundex.of(other.getLastName()))
                    && !identityKey.equals(IdentityKey.of(other))
                    && (match == null || other.getId() < match)) {
                match = other.getId();
            }
        }
        return match;
    }
}
