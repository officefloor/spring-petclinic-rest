package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The owners that the duplicate-detection and household-membership scans consider: every persisted
 * owner except those flagged soft-deleted. A soft-delete ({@code DELETE /api/owners/{id}}) retains
 * the owner's row but flags it deleted, so it must no longer take part in those scans. Defining the
 * exclusion here once keeps every such scan — the duplicate gate ({@link ValidateUniqueIdentity}
 * via {@link Household#hasMember}), the household size ({@link Household#size}) and the soft-match
 * rule ({@link PossibleDuplicate}) — agreeing on it, rather than each loop re-deciding inline.
 */
final class ActiveOwners {

    private ActiveOwners() {
    }

    /** The persisted owners not flagged soft-deleted, in the repository's iteration order. */
    static List<Owner> list(OwnerRepository ownerRepository) {
        List<Owner> active = new ArrayList<>();
        for (Owner owner : ownerRepository.findAll()) {
            if (!owner.isDeleted()) {
                active.add(owner);
            }
        }
        return active;
    }
}
