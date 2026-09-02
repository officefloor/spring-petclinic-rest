package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the id of an existing owner that shares this new owner's lastName (case-insensitively) and
 * postcode but has a different telephone — a soft, non-blocking duplicate. Runs after the identity
 * checks have already let a non-hard-duplicate through, and before the owner is saved, so
 * {@code findAll()} sees only pre-existing owners. Leaves {@code possibleDuplicateOf} null when
 * nothing matches (or the request has no postcode), which reads back as {@code possibleDuplicate}
 * false.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String telephone = owner.getTelephone();
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName.equals(normalize(existing.getLastName()))
                    && postcode.equals(existing.getPostcode())
                    && !postcode.isEmpty()
                    && !telephone.equals(existing.getTelephone())) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
