package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many owners belong to a household — owners that share a non-blank {@code householdId} —
 * and stamps that count onto {@link Owner#setHouseholdMemberCount(Integer)} just before an owner is
 * mapped to its DTO.
 *
 * <p>An owner with no {@code householdId} is a household of one. Counting reflects the owners
 * currently stored, so the freshly created owner (already saved by the time it is responded) is
 * included in its own household's count.
 */
final class HouseholdMembers {

    private HouseholdMembers() {
    }

    /** Stamp a single owner's household member count. */
    static void stamp(Owner owner, OwnerRepository ownerRepository) {
        List<Owner> all = new ArrayList<>();
        ownerRepository.findAll().forEach(all::add);
        owner.setHouseholdMemberCount(count(owner, all));
    }

    /** Stamp every owner in the collection, reading the stored owners only once. */
    static void stampAll(Collection<Owner> owners, OwnerRepository ownerRepository) {
        List<Owner> all = new ArrayList<>();
        ownerRepository.findAll().forEach(all::add);
        for (Owner owner : owners) {
            owner.setHouseholdMemberCount(count(owner, all));
        }
    }

    private static int count(Owner owner, List<Owner> all) {
        String householdId = owner.getHouseholdId();
        if (householdId == null || householdId.isBlank()) {
            return 1;
        }
        int count = 0;
        for (Owner other : all) {
            if (householdId.equals(other.getHouseholdId())) {
                count++;
            }
        }
        return count == 0 ? 1 : count;
    }
}
