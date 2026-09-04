package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects an unusually high daily signup volume: true once more than 80 owners
 * have already been registered for today's (business-day) date.
 */
public final class BulkSignup {

    /** Signups above this count for today trigger the warning. */
    private static final int THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean isWarning(Collection<Owner> existing) {
        LocalDate today = BusinessDay.rollForward(LocalDate.now());
        long todayCount = existing.stream()
            .filter(owner -> today.equals(owner.getRegistrationDate()))
            .count();
        return todayCount > THRESHOLD;
    }
}
