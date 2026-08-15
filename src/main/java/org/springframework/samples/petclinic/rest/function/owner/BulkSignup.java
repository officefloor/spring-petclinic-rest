package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner's response should carry the bulk-signup warning: it is raised once more
 * than 80 owners already share the owner's {@code registrationDate}, i.e. more than 80 owners have
 * been created for that (business) day. The threshold is strictly greater than 80 — the 81st owner
 * dated that day is the first to trip it. Counting is by the owner's stored registration day, the
 * same day {@link CheckOwnerDailyLimit} buckets creates against, so the create and read responses
 * agree.
 */
final class BulkSignup {

    /** Warn once more than this many owners share a single registration day. */
    private static final long WARN_ABOVE = 80;

    private BulkSignup() {
    }

    /** True when more than 80 owners (this one included) carry the owner's registration day. */
    static boolean warningFor(Owner owner, OwnerRepository ownerRepository) {
        LocalDate day = owner.getRegistrationDate();
        if (day == null) {
            return false;
        }
        long createdThatDay = ownerRepository.findAll().stream()
                .filter(existing -> day.equals(existing.getRegistrationDate()))
                .count();
        return createdThatDay > WARN_ABOVE;
    }
}
