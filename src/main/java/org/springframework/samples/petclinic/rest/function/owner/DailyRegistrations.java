package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counting of owners by their business-day {@link Owner#getRegistrationDate() registrationDate}.
 * The per-day create limit ({@link EnsureDailyLimit}) and the response's bulk-signup warning both
 * accumulate over this same day count, so they share this logic rather than re-walking the owners
 * each in their own way.
 */
public final class DailyRegistrations {

    /**
     * Once more than this many owners are registered on a single day, a create response carries a
     * bulk-signup warning. Below the {@link EnsureDailyLimit#MAX_OWNERS_PER_DAY hard daily limit},
     * so the warning flags a busy day before the day is closed to further creates.
     */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    private DailyRegistrations() {
    }

    /** The number of owners already registered on {@code day}. */
    static int countOn(OwnerRepository ownerRepository, LocalDate day) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }

    /** Whether {@code day} is busy enough to warn about a bulk signup. */
    static boolean isBulkSignup(OwnerRepository ownerRepository, LocalDate day) {
        return countOn(ownerRepository, day) > BULK_SIGNUP_THRESHOLD;
    }
}
