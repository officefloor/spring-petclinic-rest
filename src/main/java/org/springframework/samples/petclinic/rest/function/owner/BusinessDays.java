package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Business-day rules for owner registration.
 *
 * <p>A registration date must fall on a business day. When the effective date (whether supplied in
 * the request or defaulted to the server date) is a Saturday or Sunday, or a listed public holiday,
 * it is rolled forward to the next non-holiday weekday; an ordinary weekday is left unchanged.
 */
public final class BusinessDays {

    /** Fixed public holidays the business-day roll skips. */
    static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /**
     * The registration date an owner will actually be stored with: the supplied date, or the
     * server's current date when none was supplied, rolled off a weekend onto a business day.
     * Single source of truth shared by every step that needs the effective date, so they cannot
     * drift apart.
     */
    public static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /**
     * The number of owners already registered on the given business day. Single source of truth for
     * the per-day signup count, shared by the daily-limit and bulk-signup rules so their counts
     * cannot drift apart; each rule keeps only its own threshold and action. Counts persisted owners
     * only, so a step calling this before {@code SaveOwner} does not count the owner being created.
     */
    public static long createdOn(OwnerRepository ownerRepository, LocalDate businessDay) {
        return ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
    }

    /**
     * Roll a date forward over weekends and listed public holidays to the next business day;
     * an ordinary weekday that is not a holiday is returned unchanged.
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == DayOfWeek.SATURDAY
                || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }
}
