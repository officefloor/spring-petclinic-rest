package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Enforces the per-day create limit: at most {@link #LIMIT} owners may be
 * registered on any single day (compared by {@code registrationDate}).
 */
final class DailyRegistrationLimit {

    static final int LIMIT = 100;

    private DailyRegistrationLimit() {
    }

    /**
     * Returns {@code true} when {@link #LIMIT} or more of the {@code existing} owners were
     * already registered today, so no further owner may be created today.
     */
    static boolean isReached(Collection<Owner> existing) {
        LocalDate today = LocalDate.now();
        long createdToday = existing.stream()
            .filter(other -> today.equals(other.getRegistrationDate()))
            .count();
        return createdToday >= LIMIT;
    }
}
