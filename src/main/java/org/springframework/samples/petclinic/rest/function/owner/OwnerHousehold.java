package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Collection;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Component;

/**
 * Computes and stamps each owner's household-derived fields — the {@code householdSize} (the number of
 * owners sharing its {@code householdId}, see {@link AssignHousehold}, counting the owner itself) and
 * the {@code householdMaxMembershipLevel} (the maximum membershipLevel among the owner's <em>other</em>
 * household members, or {@code null} when there is none).
 *
 * <p>Both values depend on <em>other</em> owners, so they cannot be derived from a single {@link Owner}
 * inside the mapper. This component owns that repository lookup and writes the results onto the
 * (transient) {@link Owner#getHouseholdSize()} and {@link Owner#getHouseholdMaxMembershipLevel()} before
 * the owner is mapped, so {@code OwnerMapper} stays a pure transformation. An owner with no
 * {@code householdId} is a single-member household with no level ceiling.
 */
@Component
public class OwnerHousehold {

    private final OwnerRepository ownerRepository;

    public OwnerHousehold(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /** Stamp a single owner's household fields, computed against all current owners. */
    public Owner stamp(Owner owner) {
        if (owner != null) {
            Collection<Owner> all = ownerRepository.findAll();
            Map<String, Long> counts = householdCounts(all);
            owner.setHouseholdSize(sizeIn(owner, counts));
            owner.setHouseholdMaxMembershipLevel(maxMemberLevel(owner, all, counts));
        }
        return owner;
    }

    /** Stamp every owner in the collection, reading all owners once for the whole batch. */
    public <C extends Collection<Owner>> C stampAll(C owners) {
        if (owners != null && !owners.isEmpty()) {
            Collection<Owner> all = ownerRepository.findAll();
            Map<String, Long> counts = householdCounts(all);
            owners.forEach(owner -> {
                owner.setHouseholdSize(sizeIn(owner, counts));
                owner.setHouseholdMaxMembershipLevel(maxMemberLevel(owner, all, counts));
            });
        }
        return owners;
    }

    /** Number of owners grouped by household id. */
    private Map<String, Long> householdCounts(Collection<Owner> all) {
        return all.stream()
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

    /**
     * The maximum membershipLevel among the owner's <em>other</em> household members, or {@code null}
     * when the owner has no {@code householdId} or is the only member. Each other member's level is its
     * <em>raw</em> (uncapped) level — so the ceiling never depends on itself — computed as a pure read
     * from that member's own household size (from the same pre-computed counts), without mutating it.
     */
    private Integer maxMemberLevel(Owner owner, Collection<Owner> all, Map<String, Long> counts) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return null;
        }
        OptionalInt max = all.stream()
                .filter(other -> householdId.equals(other.getHouseholdId()))
                .filter(other -> !isSameOwner(other, owner))
                .mapToInt(other -> MembershipLevel.level(MembershipLevel.points(other, sizeIn(other, counts))))
                .max();
        return max.isPresent() ? max.getAsInt() : null;
    }

    /** Whether the two references denote the same owner (same instance or same persisted id). */
    private boolean isSameOwner(Owner a, Owner b) {
        return a == b || (a.getId() != null && a.getId().equals(b.getId()));
    }
}
