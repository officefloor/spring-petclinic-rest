package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * The registration date must fall on a business day. Whatever the owner's effective
 * registration date — supplied in the request or defaulted by {@link BuildOwner} — a
 * Saturday rolls forward two days and a Sunday one day to the following Monday, stored
 * back on the owner. Weekdays are left unchanged. Everything derived from the
 * registration date (the membership number's year segment, the daily create-limit)
 * reads it back off the owner, so it sees the adjusted date.
 */
public class NormalizeOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        DayOfWeek day = registrationDate.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            owner.setRegistrationDate(registrationDate.plusDays(2));
        } else if (day == DayOfWeek.SUNDAY) {
            owner.setRegistrationDate(registrationDate.plusDays(1));
        }
    }
}
