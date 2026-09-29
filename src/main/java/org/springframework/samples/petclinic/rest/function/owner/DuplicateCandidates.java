package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The visibility rule shared by the create endpoint's duplicate and identity checks: a soft-deleted
 * owner (see {@link Owner#isDeleted()}) is no longer a match candidate, so both
 * {@link RequireUniqueIdentity} (identity/household duplicate) and {@link AssignOwnerPossibleDuplicate}
 * (soft-match flagging) skip it. Keeping the rule in one place means the two scans that make up the
 * create endpoint's duplicate handling cannot drift apart.
 *
 * <p>Deliberately narrow: this applies only to duplicate/identity matching, not to every scan over
 * existing owners. {@code findById} is unaffected, so {@code GET /api/owners/{id}} still returns a
 * soft-deleted owner, and quota/derived-value scans keep their own (unchanged) semantics.
 */
final class DuplicateCandidates {

    private DuplicateCandidates() {
    }

    /** Whether an existing owner counts as a candidate for duplicate/identity matching. */
    static boolean isCandidate(Owner existing) {
        return !existing.isDeleted();
    }
}
