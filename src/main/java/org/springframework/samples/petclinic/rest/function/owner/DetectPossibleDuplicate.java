package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate before the new owner is saved. A hard duplicate (matching
 * telephone) has already been rejected by {@link RequireUniqueIdentity}; this records
 * the id of an existing owner that shares this owner's lastName (case-insensitively)
 * and postcode but has a different telephone, so the response can surface
 * {@code possibleDuplicate}/{@code possibleDuplicateOf}. Leaves the field null when
 * there is no postcode or no soft match.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String lastName = normalise(owner.getLastName());
        for (Owner existing : ownerRepository.findAll()) {
            if (postcode.equals(existing.getPostcode())
                    && lastName.equals(normalise(existing.getLastName()))
                    && !postcode.isBlank()
                    && !equalsTelephone(owner, existing)) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static boolean equalsTelephone(Owner owner, Owner existing) {
        String telephone = owner.getTelephone();
        return telephone != null && telephone.equals(existing.getTelephone());
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
