package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Ensures the effective registration date falls on a business day: a Saturday or Sunday rolls
 * forward to the next Monday. Runs after {@code BuildOwner} has resolved the date (supplied in the
 * request or defaulted to today) and before any step reads it, so every value derived from it — the
 * membership number's year segment and the daily create-limit count — uses the adjusted date.
 */
public class AdjustOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            owner.setRegistrationDate(date.plusDays(2));
        }
        else if (day == DayOfWeek.SUNDAY) {
            owner.setRegistrationDate(date.plusDays(1));
        }
    }
}
