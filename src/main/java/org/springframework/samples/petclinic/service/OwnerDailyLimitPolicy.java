package org.springframework.samples.petclinic.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: no more than {@value #DAILY_LIMIT} owners may be created on a single day
 * (by registrationDate). Kept as a small, self-contained unit so the rule can be enforced from
 * the create flow without adding complexity to the controller or service.
 */
public final class OwnerDailyLimitPolicy {

    private static final int DAILY_LIMIT = 100;

    private OwnerDailyLimitPolicy() {
    }

    /**
     * Reject the owner being created if {@value #DAILY_LIMIT} or more owners already share today's
     * registration date.
     *
     * @param clinicService source of the existing owners
     * @param owner the owner being created, whose (business-day) registration date is the target day
     * @throws DailyLimitReachedException if today's create limit has already been reached
     */
    public static void rejectWhenDailyLimitReached(ClinicService clinicService, Owner owner) {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : clinicService.findAllOwners()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyLimitReachedException();
        }
    }

    /** Thrown when {@value #DAILY_LIMIT} or more owners already share today's registration date. */
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class DailyLimitReachedException extends RuntimeException {
    }
}
