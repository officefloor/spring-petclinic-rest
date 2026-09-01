package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Response value: the id of an earlier owner this one soft-matches, or null when there is
 * none. A soft-match shares the earlier owner's lastName and postcode but has a different
 * (normalized) telephone, so it is not a hard {@link IdentityKey} duplicate.
 */
final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    static Integer of(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || owner.getId() == null) {
            return null;
        }
        String telephone = digits(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId() < owner.getId()
                    && postcode.equals(existing.getPostcode())
                    && Objects.equals(owner.getLastName(), existing.getLastName())
                    && !telephone.equals(digits(existing.getTelephone()))) {
                return existing.getId();
            }
        }
        return null;
    }

    private static String digits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
