package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.BusinessDays;

/**
 * Counts owners registered on a given day, compared by registrationDate.
 * Used to enforce the daily create limit.
 */
final class DailyRegistrations {

    private DailyRegistrations() {
    }

    /** Count existing owners registered on the given day's business day (weekends roll forward). */
    static int countOn(Collection<Owner> existing, LocalDate day) {
        LocalDate businessDay = BusinessDays.rollForward(day);
        return (int) existing.stream().filter(o -> businessDay.equals(o.getRegistrationDate())).count();
    }
}
