package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts the owners that belong to {@code owner}'s household, i.e. those resolving to the
 * same {@link HouseholdId}. Includes {@code owner} itself, so the value is the household's
 * total membership at read time.
 */
public final class Household {

    private Household() {
    }

    public static int size(Owner owner, OwnerRepository ownerRepository) {
        String householdId = HouseholdId.of(owner);
        return (int) ownerRepository.findAll().stream()
                .filter(other -> HouseholdId.of(other).equals(householdId))
                .count();
    }
}
