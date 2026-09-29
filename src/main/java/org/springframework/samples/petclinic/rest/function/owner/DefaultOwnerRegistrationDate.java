package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Defaults and business-day-adjusts the owner {@code registrationDate} on create. An absent (null)
 * value is first set to the server's current date; then the effective date — supplied or defaulted —
 * must fall on a business day: a Saturday or Sunday is rolled forward to the following Monday. The
 * adjusted value is written back onto the (shared) {@link OwnerFieldsDto}, so every later step sees it:
 * {@link RequireDailyOwnerLimit} counts per adjusted business day, {@link BuildOwner} stores it, the
 * membership number's year segment derives from it, and {@code GET /owners/{id}} returns it in ISO
 * 'YYYY-MM-DD' format. Only wired into the create pipeline, so updates never re-stamp the date.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto owner) {
        LocalDate date = owner.getRegistrationDate();
        if (date == null) {
            date = LocalDate.now();
        }
        owner.setRegistrationDate(toBusinessDay(date));
    }

    private static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
