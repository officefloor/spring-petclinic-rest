package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Derives an owner's numeric {@code membershipLevel} (1..4). The level starts at 1,
 * gains 1 when the owner has a non-blank email, gains 1 when {@code namesakeCount} is 0,
 * gains 1 when the owner's tenure exceeds 365 days, and is capped at 4. Because a newly
 * created owner has zero tenure, a new owner never exceeds level 3. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit mapping method.
 */
public final class Membership {

    /** A tenure strictly greater than this many days is required to reach level 4. */
    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

    private Membership() {
    }

    /** The numeric membership level (1..4) for {@code owner}. */
    public static int levelOf(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS_FOR_LEVEL_4) {
            level++;
        }
        return Math.min(level, 4);
    }
}
