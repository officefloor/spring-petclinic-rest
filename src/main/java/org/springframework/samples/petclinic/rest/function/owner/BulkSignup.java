package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Bulk-signup warning rule: signals when a day's create volume has grown large. The response's
 * {@code bulkSignupWarning} is true once <em>more than 80</em> owners share the given (business-day
 * adjusted) registration date, otherwise false. Shares the same accumulation path as the daily
 * create-limit rule ({@link CheckOwnerDailyLimit}), which caps the same day at 100.
 */
final class BulkSignup {

    private static final int WARNING_THRESHOLD = 80;

    private BulkSignup() {
    }

    /**
     * Whether more than 80 owners already carry the effective business day of {@code registrationDate}.
     */
    static boolean warning(LocalDate registrationDate, OwnerRepository ownerRepository) {
        LocalDate day = BusinessDay.effective(registrationDate);
        long count = ownerRepository.findAll().stream()
            .filter(o -> day.equals(o.getRegistrationDate()))
            .count();
        return count > WARNING_THRESHOLD;
    }
}
