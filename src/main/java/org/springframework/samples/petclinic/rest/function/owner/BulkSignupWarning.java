package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the {@code bulkSignupWarning} response flag: true once more than 80 owners
 * have already been created for the current business day, otherwise false.
 *
 * <p>"Created today" is measured the same way as the per-day create-limit rule
 * ({@link EnsureDailyCapacity}): by comparing each owner's stored registration date to
 * today's business-day-adjusted date, so both rules accumulate over the same day.
 */
final class BulkSignupWarning {

    private static final int WARNING_THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    static boolean forToday(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.roll(LocalDate.now());
        long createdToday = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(today::equals)
            .count();
        return createdToday > WARNING_THRESHOLD;
    }
}
