package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Single source of truth for the business-day rule on an owner's registration date: the
 * effective registration date must fall on a business day, so a Saturday or Sunday rolls
 * forward to the following Monday. A date that is already a weekday is returned unchanged.
 *
 * <p>Centralised here so every site agrees on one definition: {@link BuildOwner} stores the
 * adjusted date on the owner (and everything derived from it, such as the membership number's
 * year segment in {@link AssignMembershipNumber}, then uses the adjusted value), while
 * {@link CheckDailyOwnerLimit} counts existing owners against the same adjusted business day.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day: a Saturday or Sunday advances to the
     * following Monday; a weekday is returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (rolled.getDayOfWeek() == DayOfWeek.SATURDAY || rolled.getDayOfWeek() == DayOfWeek.SUNDAY) {
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
