package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Numeric membership level for pet owners, from 1 to 4. Every owner starts at level 1; the level
 * gains 1 when the owner has an email address (non-null, non-blank) and a further 1 when its
 * {@code namesakeCount} is 0 (no existing owner shares its first and last name). A final level is
 * awarded for tenure: 1 more when more than 365 days have elapsed since the owner's
 * {@code registrationDate}, capped at 4. Because a newly created owner's registration date is the
 * current date, its tenure is zero, so a new owner never exceeds level 3 (an owner with an email and
 * a {@code namesakeCount} of 0 maxes the pre-tenure factors at level 3, reaching level 4 only once
 * its tenure passes 365 days). Derived purely from the owner's own stored state
 * ({@code namesakeCount} is recorded at create time), so it is seed-independent. Used by the owner
 * mapper to expose {@code membershipLevel} on responses.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership level for the given namesake count, email and registration date: 1 to start,
     * +1 when the email is present (non-null, non-blank), +1 when the namesake count is exactly 0,
     * +1 when the tenure since {@code registrationDate} exceeds 365 days, capped at 4.
     */
    public static int of(Integer namesakeCount, String email, LocalDate registrationDate) {
        int level = 1;
        boolean hasEmail = email != null && !email.isBlank();
        if (hasEmail) {
            level++;
        }
        boolean unique = namesakeCount != null && namesakeCount == 0;
        if (unique) {
            level++;
        }
        boolean tenured = registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
        if (tenured) {
            level++;
        }
        return Math.min(level, 4);
    }
}
