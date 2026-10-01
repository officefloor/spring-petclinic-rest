package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Business-day rules for owner registration.
 *
 * <p>A registration date must fall on a business day. When the effective date (whether supplied in
 * the request or defaulted to the server date) is a Saturday or Sunday, it is rolled forward to the
 * following Monday; a weekday is left unchanged.
 */
public final class BusinessDays {

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

    /** Roll a weekend date forward to the next Monday; weekdays are returned unchanged. */
    public static LocalDate toBusinessDay(LocalDate date) {
        LocalDate adjusted = date;
        while (adjusted.getDayOfWeek() == DayOfWeek.SATURDAY
                || adjusted.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }
}
