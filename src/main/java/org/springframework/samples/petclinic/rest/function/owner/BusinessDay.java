package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for the business-day rule on an owner's registration date: the
 * effective registration date must fall on a business day, so a Saturday or Sunday rolls
 * forward to the following Monday. A date that is already a weekday is returned unchanged.
 *
 * <p>A business day is additionally not one of the fixed public holidays, so a date landing on a
 * listed holiday also rolls forward to the next non-holiday business day.
 *
 * <p>Centralised here so every site agrees on one definition: {@link BuildOwner} stores the
 * adjusted date on the owner (and everything derived from it, such as the membership number's
 * year segment in {@link AssignMembershipNumber}, then uses the adjusted value), while
 * {@link CheckDailyOwnerLimit} counts existing owners against the same adjusted business day.
 */
final class BusinessDay {

    /** Fixed public holidays that are not business days; a date landing here rolls forward. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"), LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day: a Saturday, Sunday or listed public
     * holiday advances a day until a non-holiday weekday is reached; such a weekday is returned
     * unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (rolled.getDayOfWeek() == DayOfWeek.SATURDAY || rolled.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(rolled)) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }

    /**
     * Counts existing owners whose stored {@code registrationDate} equals {@code businessDay} — the
     * single definition of "owners created on this business day" shared by the create-time rules
     * ({@link CheckDailyOwnerLimit}'s hard cap and {@link AssignBulkSignupWarning}'s flag), so they
     * can never drift apart. Both call this and compare the result against their own threshold.
     */
    static long countOwnersOn(LocalDate businessDay, OwnerRepository ownerRepository) {
        return ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(businessDay::equals)
            .count();
    }
}
