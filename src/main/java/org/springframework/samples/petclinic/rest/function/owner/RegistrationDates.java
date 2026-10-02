package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared rules for an owner's registration date. The <em>effective</em> registration date is the one
 * supplied in the request, or the server's current date when none was supplied; it must always fall on
 * a business day, so a Saturday, Sunday or listed public holiday is rolled forward to the next
 * non-holiday business day. Every value derived from
 * the registration date (the stored {@code registrationDate}, the membership number's year segment and
 * the per-day create-limit bucket) is computed from this adjusted date.
 *
 * <p>The per-day owner-count policy also lives here: {@link #DAILY_LIMIT} is the hard cap enforced at
 * create time, and {@link #BULK_WARNING_THRESHOLD} is the (lower) count above which a response carries
 * a bulk-signup warning. Both tiers are counted against {@link #countOn(LocalDate, OwnerRepository)}.
 */
final class RegistrationDates {

    /**
     * Fixed public holidays that are not business days: when the weekend-adjusted date lands on one of
     * these, it is rolled forward to the next non-holiday business day.
     */
    static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    /** Maximum number of owners that may be created for a single day. */
    static final int DAILY_LIMIT = 100;

    /** Owners created for a single day beyond which a response carries a bulk-signup warning. */
    static final int BULK_WARNING_THRESHOLD = 80;

    private RegistrationDates() {
    }

    /** Count the owners whose stored registration date equals {@code businessDay}. */
    static int countOn(LocalDate businessDay, OwnerRepository repository) {
        int count = 0;
        for (Owner existing : repository.findAll()) {
            if (businessDay != null && businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Whether a {@code supplied} registration date lies in the future, i.e. later than the server's
     * current date. A {@code null} date (none supplied) is never in the future.
     */
    static boolean isFuture(LocalDate supplied) {
        return supplied != null && supplied.isAfter(LocalDate.now());
    }

    /**
     * The effective, business-day registration date: {@code supplied} when non-null, otherwise the
     * server's current date, rolled forward to the next business day via {@link #toBusinessDay} when it
     * lands on a weekend or public holiday.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /**
     * Roll a date forward to the next business day: weekends and {@link #HOLIDAYS listed public holidays}
     * are skipped, so the returned date is never a Saturday, Sunday or public holiday. A date that is
     * already a non-holiday weekday is returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
