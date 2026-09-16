package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Shared filtering for the create-owner duplicate and identity checks, which consider only
 * <em>active</em> owners — those that have not been soft-deleted (see {@link DeleteOwner}).
 * A soft-deleted owner keeps its row and is still readable, but no longer counts as a
 * duplicate of, or namesake for, a newly created owner.
 */
final class Owners {

    private Owners() {
    }

    /** The subset of {@code owners} that are still active — not soft-deleted. */
    static List<Owner> active(Iterable<Owner> owners) {
        List<Owner> active = new ArrayList<>();
        for (Owner owner : owners) {
            if (!owner.isDeleted()) {
                active.add(owner);
            }
        }
        return active;
    }
}
