package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives, on read, how many owners belong to a household — those sharing the shared
 * {@code householdId} assigned by {@link AssignOwnerHousehold}. Response functions call this
 * to populate {@link Owner#setHouseholdSize} before mapping, which keeps the household-size
 * lookup in the function layer (like {@link EnsureUniqueOwnerHousehold} and
 * {@link AssignOwnerHousehold}) and lets {@code OwnerMapper} stay a pure projection.
 */
final class OwnerHouseholdSize {

    private OwnerHouseholdSize() {
    }

    /**
     * Populates {@link Owner#setHouseholdSize} for a single owner: the number of owners sharing
     * its {@code householdId}, or 0 when it belongs to no shared household.
     */
    static void populate(Owner owner, OwnerRepository ownerRepository) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            owner.setHouseholdSize(0);
            return;
        }
        int members = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (householdId.equals(existing.getHouseholdId())) {
                members++;
            }
        }
        owner.setHouseholdSize(members);
    }

    /**
     * Populates {@link Owner#setHouseholdSize} for every owner in {@code owners} using a single
     * pass over all owners, so a collection response derives sizes without an all-owners query
     * per element.
     */
    static void populateAll(Iterable<Owner> owners, OwnerRepository ownerRepository) {
        Map<String, Integer> sizes = new HashMap<>();
        for (Owner existing : ownerRepository.findAll()) {
            String householdId = existing.getHouseholdId();
            if (householdId != null) {
                sizes.merge(householdId, 1, Integer::sum);
            }
        }
        for (Owner owner : owners) {
            String householdId = owner.getHouseholdId();
            owner.setHouseholdSize(householdId == null ? 0 : sizes.getOrDefault(householdId, 0));
        }
    }
}
