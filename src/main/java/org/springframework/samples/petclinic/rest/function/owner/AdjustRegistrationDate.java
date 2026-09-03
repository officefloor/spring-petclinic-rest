package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rolls the effective registrationDate (supplied or defaulted) forward to the next
 * Monday when it lands on a weekend, before {@link CheckDailyLimit} counts by it and the
 * membership number derives its year segment from it.
 */
public class AdjustRegistrationDate {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        LocalDate date = owner.getRegistrationDate();
        if (date.isAfter(LocalDate.now())) {
            throw new MissingOwnerFieldsException(List.of("registrationDate"));
        }
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        owner.setRegistrationDate(date);
    }
}
