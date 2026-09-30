package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipLevel}, a numeric level from 1 to 4 fixed at
 * create time: it starts at 1, gains 1 when an email is present, 1 more when the owner
 * has no namesakes ({@code namesakeCount} of 0), and 1 more when the owner's tenure is
 * more than 365 days, capped at 4. Tenure is the number of whole days between the
 * registration date and today; a newly created owner has zero tenure, so a new owner
 * never exceeds level 3. Runs after the namesake count has been assigned, so that value
 * is available.
 */
public class AssignMembershipLevel {

    static final int MAX_LEVEL = 4;

    /** Level 4 requires tenure of more than this many days. */
    static final long TENURE_DAYS_FOR_TOP_LEVEL = 365;

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
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > TENURE_DAYS_FOR_TOP_LEVEL) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }
}
