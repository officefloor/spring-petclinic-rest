package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Rolls the effective registrationDate (supplied or defaulted) forward to the next
 * Monday when it lands on a weekend, before {@link CheckDailyLimit} counts by it and the
 * membership number derives its year segment from it.
 */
public class AdjustRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        owner.setRegistrationDate(date);
    }
}
