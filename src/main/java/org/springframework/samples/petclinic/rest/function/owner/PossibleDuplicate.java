package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Finds an existing owner that {@code owner} possibly duplicates: another owner sharing this
 * one's {@link Soundex} of the last name and postcode but whose {@link IdentityKey} differs — a
 * soft match that is not a hard identity duplicate (a differing telephone is enough to make the
 * keys differ). Returns the earliest such owner's id, or {@code null} when there is none.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    public static Integer of(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = IdentityKey.of(owner);
        return ownerRepository.findAll().stream()
                .filter(other -> other.getId() != null && !other.getId().equals(owner.getId()))
                .filter(other -> !identityKey.equals(IdentityKey.of(other)))
                .filter(other -> postcode.equals(other.getPostcode()))
                .filter(other -> soundex.equals(Soundex.of(other.getLastName())))
                .map(Owner::getId)
                .min(Integer::compareTo)
                .orElse(null);
    }
}
