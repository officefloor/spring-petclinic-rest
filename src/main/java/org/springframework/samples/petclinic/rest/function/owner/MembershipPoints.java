package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Points score behind a pet owner's membership. Every owner starts at 0 points; the score gains 2
 * when the owner has an email address (non-null, non-blank), a further 1 when its
 * {@code namesakeCount} is 0 (no existing owner shares its first and last name), a further 2 when
 * its {@code householdSize} is 3 or more, and a further 3 when more than 365 days have elapsed since
 * the owner's {@code registrationDate}. Because a newly created owner's registration date is the
 * current date, its tenure is zero, so a new owner never earns the tenure points. Derived purely
 * from the owner's own stored state ({@code namesakeCount} and {@code householdSize} are recorded at
 * create time), so it is seed-independent. Used by the owner mapper to expose
 * {@code membershipPoints} on responses and, via {@link MembershipLevel}, {@code membershipLevel}.
 */
public final class MembershipPoints {

    private MembershipPoints() {
    }

    /**
     * The membership points for the given namesake count, email, household size and registration
     * date: 0 to start, +2 when the email is present (non-null, non-blank), +1 when the namesake
     * count is exactly 0, +2 when the household size is 3 or more, +3 when the tenure since
     * {@code registrationDate} exceeds 365 days.
     */
    public static int of(Integer namesakeCount, String email, Integer householdSize,
            LocalDate registrationDate) {
        int points = 0;
        boolean hasEmail = email != null && !email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = namesakeCount != null && namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = householdSize != null && householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = registrationDate != null
                && ChronoUnit.DAYS.between(registrationDate, LocalDate.now()) > 365;
        if (tenured) {
            points += 3;
        }
        return points;
    }
}
