package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The bulk-signup warning policy: whether today's owner registrations have grown unusually
 * high. It shares the same per-day accumulation as the create limit ({@link EnsureDailyLimit}),
 * counting owners by registration date on the current business day, and is surfaced as the
 * read-only {@code bulkSignupWarning} flag on every owner response.
 */
final class BulkSignup {

    /** Registering more than this many owners on a single business day raises the warning. */
    static final long BULK_SIGNUP_THRESHOLD = 80;

    private BulkSignup() {
    }

    /**
     * Whether more than {@link #BULK_SIGNUP_THRESHOLD} owners have already been registered for
     * the current business day (weekends rolled forward, matching {@link ResolveRegistrationDate}).
     */
    static boolean warningRaised(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDays.rollForward(LocalDate.now());
        return DailyRegistrations.countOn(ownerRepository, today) > BULK_SIGNUP_THRESHOLD;
    }
}
