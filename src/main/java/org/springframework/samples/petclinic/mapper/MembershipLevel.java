package org.springframework.samples.petclinic.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric membership level (1 to 4) from their persisted fields.
 *
 * <p>Starts at 1; adds 1 when an email is present; adds 1 when {@code namesakeCount} is 0; adds 1
 * when tenure exceeds 365 days. Tenure is the number of days from the registration date to today,
 * so a newly created owner has zero tenure and never earns the fourth level: the pre-tenure factors
 * alone cap at 3. Level 4 is reachable only once an owner's registration date is more than a year
 * in the past.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method. Single source of truth so the response DTO and
 * the create audit line agree on the level.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /** The membership level (1 to 4) for {@code owner}. */
    public static int of(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        // Level 4 requires tenure of more than 365 days; a new owner's zero tenure caps them at 3.
        level = Math.min(3, level);
        if (owner.getRegistrationDate() != null
                && ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now()) > 365) {
            level++;
        }
        return level;
    }
}
