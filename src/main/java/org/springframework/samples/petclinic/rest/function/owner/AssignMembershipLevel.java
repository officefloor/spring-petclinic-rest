package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} on create. Starts at 1, adds 1
 * when an email address is present, adds 1 when {@code namesakeCount} is 0, and adds 1
 * when the owner belongs to a household of three or more members. Those factors can
 * raise the raw level to 4, but <strong>level 4 requires tenure of more than 365
 * days</strong>: when tenure is 365 days or less the level is capped at 3. A newly
 * created owner has zero tenure (its registration date is the server date), so a new
 * owner never exceeds level 3 — a new owner with an email, a {@code namesakeCount} of 0
 * and a 3-member household is level 3, not 4. Runs after {@code namesakeCount} and
 * {@code householdSize} have been counted and before the owner is saved, so the level
 * is fixed at creation time.
 */
public class AssignMembershipLevel {

    private static final long TENURE_DAYS_FOR_LEVEL_4 = 365;

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
        Integer householdSize = owner.getHouseholdSize();
        if (householdSize != null && householdSize >= 3) {
            level++;
        }
        // Level 4 requires tenure of more than 365 days; otherwise cap at 3.
        if (level >= 4 && tenureDays(owner) <= TENURE_DAYS_FOR_LEVEL_4) {
            level = 3;
        }
        owner.setMembershipLevel(level);
    }

    private static long tenureDays(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }
}
