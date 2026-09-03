package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.BusinessDays;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Flags a bulk signup: {@code true} once more than {@link #THRESHOLD} owners have
 * already been created today (compared by {@code registrationDate}).
 */
final class BulkSignupWarning {

    static final int THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    /**
     * Returns {@code true} when more than {@link #THRESHOLD} of the {@code existing}
     * owners were already registered today.
     */
    static boolean isTriggered(Collection<Owner> existing) {
        LocalDate today = BusinessDays.roll(LocalDate.now());
        long createdToday = existing.stream()
            .filter(other -> today.equals(other.getRegistrationDate()))
            .count();
        return createdToday > THRESHOLD;
    }
}
