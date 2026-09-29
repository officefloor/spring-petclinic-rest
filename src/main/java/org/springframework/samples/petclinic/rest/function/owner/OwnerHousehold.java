package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Computes and stamps each owner's household size — the number of owners sharing its
 * {@code householdId} (see {@link AssignHousehold}), counting the owner itself.
 *
 * <p>The size depends on <em>other</em> owners, so it cannot be derived from a single {@link Owner}
 * inside the mapper. This component owns that repository lookup and writes the result onto the
 * (transient) {@link Owner#getHouseholdSize()} before the owner is mapped, so {@code OwnerMapper}
 * stays a pure transformation. An owner with no {@code householdId} is a single-member household.
 */
@Component
public class OwnerHousehold {

    private final OwnerRepository ownerRepository;

    public OwnerHousehold(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /** Stamp a single owner's household size, computed against all current owners. */
    public Owner stamp(Owner owner) {
        if (owner != null) {
            owner.setHouseholdSize(sizeIn(owner, householdCounts()));
        }
        return owner;
    }

    /** Stamp every owner in the collection, reading all owners once for the whole batch. */
    public <C extends Collection<Owner>> C stampAll(C owners) {
        if (owners != null && !owners.isEmpty()) {
            Map<String, Long> counts = householdCounts();
            owners.forEach(owner -> owner.setHouseholdSize(sizeIn(owner, counts)));
        }
        return owners;
    }

    /** Number of existing owners grouped by household id. */
    private Map<String, Long> householdCounts() {
        return ownerRepository.findAll().stream()
                .filter(existing -> existing.getHouseholdId() != null)
                .collect(Collectors.groupingBy(Owner::getHouseholdId, Collectors.counting()));
    }

    /** The owner's household size from pre-computed counts, always at least 1 (the owner itself). */
    private int sizeIn(Owner owner, Map<String, Long> counts) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        return (int) Math.max(counts.getOrDefault(householdId, 0L), 1L);
    }
}
