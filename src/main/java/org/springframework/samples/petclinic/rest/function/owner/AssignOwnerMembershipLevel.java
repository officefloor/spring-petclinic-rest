package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} (1 to 4) at creation time.
 *
 * <p>The level starts at 1 and gains one for each qualifying factor: a non-blank email address, a
 * {@code namesakeCount} of 0, and tenure of more than {@value #TENURE_THRESHOLD_DAYS} days
 * (measured from the owner's {@code registrationDate}). Level 4 therefore requires tenure past that
 * threshold. A newly created owner registers as of the current date and so has zero tenure, which
 * means the tenure factor never applies at creation: a new owner never exceeds level 3 — even one
 * with an email, a {@code namesakeCount} of 0 and a multi-member household lands at level 3, not 4.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link CountOwnerNamesakes} (so
 * {@code namesakeCount} is already frozen) and before {@link SaveOwner} (so the value is persisted)
 * and {@link AuditOwnerCreated} (so the audit line can record it). Frozen at creation and returned
 * unchanged on later reads.
 */
public class AssignOwnerMembershipLevel {

    /** Tenure, in days, that an owner must exceed to earn the level-4 factor. */
    static final long TENURE_THRESHOLD_DAYS = 365;

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        if (tenureDays(owner) > TENURE_THRESHOLD_DAYS) {
            level++;
        }
        owner.setMembershipLevel(level);
    }

    /**
     * The owner's tenure in whole days: the span from their {@code registrationDate} to the current
     * date, never negative, and 0 when no registration date is recorded. A just-created owner
     * registers as of today, so its tenure is 0.
     */
    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(registrationDate, LocalDate.now()), 0);
    }
}
