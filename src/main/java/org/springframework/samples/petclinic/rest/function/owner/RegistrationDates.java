package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Registration-date business rule: the effective registration date must fall on a business day.
 * A Saturday or Sunday, or a listed public holiday, rolls forward to the next non-holiday business
 * day. Shared by {@link BuildOwner} (which stamps the stored date) and {@link CheckOwnerDailyLimit}
 * (which counts per business day) so both agree on the adjusted date. It also owns the
 * per-business-day owner count, so the daily-limit rule and the bulk-signup warning
 * ({@link DeriveOwnerBulkSignupWarning}) provably count the same population.
 */
final class RegistrationDates {

    /** Fixed public-holiday calendar; these dates are never business days. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private RegistrationDates() {
    }

    /** The number of existing owners whose stored registration date equals {@code day}. */
    static long countRegisteredOn(LocalDate day, OwnerRepository ownerRepository) {
        return ownerRepository.findAll().stream()
                .filter(existing -> day != null && day.equals(existing.getRegistrationDate()))
                .count();
    }

    /**
     * The effective registration date: the supplied date, or the server's current date when none
     * was supplied, rolled forward off a weekend to the next business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /**
     * Roll a weekend or public holiday forward to the next non-holiday business day; any other day
     * is returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
