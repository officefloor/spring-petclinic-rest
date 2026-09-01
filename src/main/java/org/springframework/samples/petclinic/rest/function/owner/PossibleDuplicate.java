package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Response value: the id of an earlier owner this one soft-matches, or null when there is
 * none. A soft-match shares the earlier owner's postcode and {@code soundex(lastName)} but
 * has a different {@link IdentityKey}, so it is not a hard duplicate.
 */
final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    static Integer of(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || owner.getId() == null) {
            return null;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.of(owner);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId() < owner.getId()
                    && postcode.equals(existing.getPostcode())
                    && soundex.equals(Soundex.of(existing.getLastName()))
                    && !identityKey.equals(IdentityKey.of(existing))) {
                return existing.getId();
            }
        }
        return null;
    }
}
