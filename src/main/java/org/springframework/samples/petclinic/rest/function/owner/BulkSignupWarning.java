package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single definition of the bulk-signup warning: it is raised once more than
 * {@link #THRESHOLD} owners have already been created on today's business day (counted by
 * {@link OwnersCreatedToday}). Surfaced on every owner response so callers can spot an
 * unusually high signup volume before the per-day create limit is reached.
 */
final class BulkSignupWarning {

    /** Owners created today above which the warning is raised. */
    static final int THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    /** Whether more than {@link #THRESHOLD} owners have been created on today's business day. */
    static boolean isRaised(OwnerRepository ownerRepository) {
        return OwnersCreatedToday.count(ownerRepository) > THRESHOLD;
    }
}
