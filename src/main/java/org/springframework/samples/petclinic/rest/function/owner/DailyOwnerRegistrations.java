package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared accumulation path for the per-business-day owner-registration rules. Counts the owners
 * whose {@code registrationDate} falls on the current business day (weekends roll forward to the
 * next Monday, see {@link BusinessDay}), the same bucket used by both the daily create-limit
 * ({@link CheckDailyOwnerLimit}) and the bulk-signup warning surfaced on {@link org
 * .springframework.samples.petclinic.rest.dto.OwnerDto}.
 */
final class DailyOwnerRegistrations {

    /** Owners beyond this on the current business day trigger the bulk-signup warning. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    private DailyOwnerRegistrations() {
    }

    /** Number of owners registered on the current business day. */
    static int countToday(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.rollForward(LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }

    /** True when more than {@link #BULK_SIGNUP_THRESHOLD} owners were created on the current business day. */
    static boolean bulkSignupWarning(OwnerRepository ownerRepository) {
        return countToday(ownerRepository) > BULK_SIGNUP_THRESHOLD;
    }
}
