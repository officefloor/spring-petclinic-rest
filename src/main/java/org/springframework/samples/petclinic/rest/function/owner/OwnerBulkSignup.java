package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Read-time derivation of an owner's {@code bulkSignupWarning}: {@code true} once MORE THAN
 * {@value #BULK_SIGNUP_THRESHOLD} owners share the given registration day, otherwise {@code false}.
 *
 * <p>The day count uses the same per-day accumulation path exercised by
 * {@link EnsureDailyOwnerLimit} — owners are counted by {@link Owner#getRegistrationDate()} against
 * the supplied day, so a freshly created owner's response reflects how many owners were created on
 * its (adjusted) business day.
 */
public final class OwnerBulkSignup {

    /** Owners created in a single day beyond this count raise the bulk-signup warning. */
    public static final int BULK_SIGNUP_THRESHOLD = 80;

    private OwnerBulkSignup() {
    }

    /**
     * Whether more than {@link #BULK_SIGNUP_THRESHOLD} owners have been created on
     * {@code registrationDate}. Returns {@code false} when the date is unknown.
     */
    public static boolean isWarning(OwnerRepository ownerRepository, LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > BULK_SIGNUP_THRESHOLD;
    }
}
