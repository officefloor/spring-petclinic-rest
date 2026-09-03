package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Finds an existing owner that {@code owner} possibly duplicates: another owner sharing this
 * one's last name (case-insensitive) and postcode but with a different telephone — a soft match
 * that is not a hard {@link IdentityKey} duplicate. Owners in the same {@link HouseholdId} are
 * excluded: a declared household member is not a suspected duplicate. Returns the earliest such
 * owner's id, or {@code null} when there is none.
 */
public final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    public static Integer of(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String householdId = HouseholdId.of(owner);
        return ownerRepository.findAll().stream()
                .filter(other -> other.getId() != null && !other.getId().equals(owner.getId()))
                .filter(other -> !householdId.equals(HouseholdId.of(other)))
                .filter(other -> postcode.equals(other.getPostcode()))
                .filter(other -> other.getLastName().equalsIgnoreCase(owner.getLastName()))
                .filter(other -> !owner.getTelephone().equals(other.getTelephone()))
                .map(Owner::getId)
                .min(Integer::compareTo)
                .orElse(null);
    }
}
