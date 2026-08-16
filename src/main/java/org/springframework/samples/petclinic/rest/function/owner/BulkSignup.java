package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the {@code bulkSignupWarning} response flag: {@code true} once more than
 * {@value #WARN_ABOVE} owners have already been created for the current business day, flagging a
 * high-volume sign-up day before the harder per-day cap enforced by {@link CheckDailyLimit} is hit.
 *
 * <p>"Created today" is counted by {@code registrationDate} equal to the current business day, using
 * the same weekend roll-forward as {@link ResolveRegistrationDate} so the count lines up exactly with
 * the dates newly created owners are stored under.
 */
public final class BulkSignup {

    /** Owners created today beyond this count raise the warning ({@code count > WARN_ABOVE}). */
    static final int WARN_ABOVE = 80;

    private BulkSignup() {
    }

    /** Whether more than {@value #WARN_ABOVE} owners already exist for the current business day. */
    public static boolean warningFor(OwnerRepository ownerRepository) {
        LocalDate today = LocalDate.now();
        // A weekend rolls forward to the next Monday, matching ResolveRegistrationDate.
        while (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            today = today.plusDays(1);
        }
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count > WARN_ABOVE;
    }
}
