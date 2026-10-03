package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Business-day helper for owner registration. A registration date must fall on a
 * business day: a Saturday or Sunday rolls forward to the following Monday. Single
 * source of truth for the effective registration date and the per-business-day owner
 * count, so every caller (the stored value, the daily create-limit and the bulk-signup
 * warning) agrees.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Counts the existing owners whose registration date equals {@code businessDay}.
     * The single source of truth for "how many owners were created on a business day",
     * so per-day rules (the create-limit and the bulk-signup warning) cannot drift
     * apart. The caller passes a day already resolved via
     * {@link #effectiveRegistrationDate(LocalDate)}.
     */
    static long countCreatedOn(OwnerRepository ownerRepository, LocalDate businessDay) {
        return ownerRepository.findAll().stream()
            .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
            .count();
    }

    /**
     * The effective registration date: {@code supplied} when present, otherwise the
     * server's current date, rolled forward over a weekend to the following Monday.
     */
    static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollToBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /** Rolls {@code date} forward to the next business day: a weekend becomes Monday. */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
