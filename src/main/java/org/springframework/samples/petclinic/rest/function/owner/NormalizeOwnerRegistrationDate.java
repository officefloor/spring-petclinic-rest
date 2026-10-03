package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * The registration date must fall on a business day. Whatever the owner's effective
 * registration date — supplied in the request or defaulted by {@link BuildOwner} — a
 * Saturday rolls forward two days and a Sunday one day to the following Monday, stored
 * back on the owner. Weekdays are left unchanged. The rule extends to public holidays:
 * when the adjusted date lands on a listed public holiday, it rolls forward to the next
 * non-holiday business day. Everything derived from the registration date (the
 * membership number's year segment, the daily create-limit) reads it back off the owner,
 * so it sees the adjusted date.
 */
public class NormalizeOwnerRegistrationDate {

    /** Fixed public-holiday calendar: dates that are never valid registration days. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    public void service(@Val Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        while (!isBusinessDay(registrationDate)) {
            registrationDate = registrationDate.plusDays(1);
        }
        owner.setRegistrationDate(registrationDate);
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
                && !PUBLIC_HOLIDAYS.contains(date);
    }
}
