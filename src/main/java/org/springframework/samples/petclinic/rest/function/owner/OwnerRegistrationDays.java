package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of how owners are grouped by their registration day: matched by exact
 * date. Shared by {@link RejectDailyOwnerLimit} (which caps a day's registrations) and
 * {@link AssignOwnerBulkSignupWarning} (which flags a busy day), so both count a day's members
 * identically.
 */
final class OwnerRegistrationDays {

    private OwnerRegistrationDays() {
    }

    /** The number of existing owners registered on {@code date}. */
    static int size(Iterable<Owner> owners, LocalDate date) {
        int count = 0;
        for (Owner existing : owners) {
            if (date != null && date.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }
}
